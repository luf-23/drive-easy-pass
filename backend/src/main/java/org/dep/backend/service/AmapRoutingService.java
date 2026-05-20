package org.dep.backend.service;

import org.dep.backend.dto.DrivingRouteResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.dep.backend.util.GeoCoordTransform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AmapRoutingService {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    @Value("${app.amap.web-key:}")
    private String webKey;

    public boolean isConfigured() {
        return webKey != null && !webKey.isBlank();
    }

    public DrivingRouteResponse driving(
            double originLng,
            double originLat,
            double destLng,
            double destLat,
            List<double[]> waypointLngLat
    ) {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "未配置高德 Web 服务 Key（app.amap.web-key），无法按道路规划路线");
        }

        double[] o = GeoCoordTransform.wgs84ToGcj02(originLng, originLat);
        double[] d = GeoCoordTransform.wgs84ToGcj02(destLng, destLat);
        String origin = formatCoord(o[0], o[1]);
        String destination = formatCoord(d[0], d[1]);
        StringBuilder urlBuilder = new StringBuilder("https://restapi.amap.com/v3/direction/driving?key=")
                .append(URLEncoder.encode(webKey.trim(), StandardCharsets.UTF_8))
                .append("&origin=").append(origin)
                .append("&destination=").append(destination)
                .append("&extensions=all&strategy=0");
        if (waypointLngLat != null && !waypointLngLat.isEmpty()) {
            StringBuilder wp = new StringBuilder();
            for (double[] point : waypointLngLat) {
                double[] g = GeoCoordTransform.wgs84ToGcj02(point[0], point[1]);
                if (!wp.isEmpty()) {
                    wp.append(';');
                }
                wp.append(formatCoord(g[0], g[1]));
            }
            urlBuilder.append("&waypoints=").append(wp);
        }
        String url = urlBuilder.toString();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(12))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("高德路径规划请求失败: HTTP " + response.statusCode());
            }
            return parseResponse(response.body());
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("高德路径规划调用异常: " + ex.getMessage(), ex);
        }
    }

    private DrivingRouteResponse parseResponse(String body) throws Exception {
        JsonNode root = jsonMapper.readTree(body);
        if (!"1".equals(root.path("status").asText())) {
            String info = root.path("info").asText("未知错误");
            throw new IllegalStateException("高德返回错误: " + info);
        }
        JsonNode paths = root.path("route").path("paths");
        if (!paths.isArray() || paths.isEmpty()) {
            throw new IllegalStateException("高德未返回可用路线");
        }
        JsonNode first = paths.get(0);
        List<double[]> path = parseStepsPolylines(first.path("steps"));
        if (path.size() < 2) {
            throw new IllegalStateException("高德路线折线点数不足");
        }
        int distance = first.path("distance").asInt(0);
        int duration = first.path("duration").asInt(0);
        return new DrivingRouteResponse(path, distance, duration, "amap");
    }

    private List<double[]> parseStepsPolylines(JsonNode steps) {
        List<double[]> points = new ArrayList<>();
        if (!steps.isArray()) {
            return points;
        }
        for (JsonNode step : steps) {
            String polyline = step.path("polyline").asText("");
            if (polyline.isBlank()) {
                continue;
            }
            for (String pair : polyline.split(";")) {
                if (pair.isBlank()) {
                    continue;
                }
                String[] parts = pair.split(",");
                if (parts.length < 2) {
                    continue;
                }
                double lng = Double.parseDouble(parts[0].trim());
                double lat = Double.parseDouble(parts[1].trim());
                appendPoint(points, lat, lng);
            }
        }
        return points;
    }

    private void appendPoint(List<double[]> points, double lat, double lng) {
        if (points.isEmpty()) {
            points.add(new double[] {lat, lng});
            return;
        }
        double[] last = points.get(points.size() - 1);
        if (Math.abs(last[0] - lat) < 1e-6 && Math.abs(last[1] - lng) < 1e-6) {
            return;
        }
        points.add(new double[] {lat, lng});
    }

    private static String formatCoord(double lng, double lat) {
        return String.format(Locale.US, "%.6f,%.6f", lng, lat);
    }
}
