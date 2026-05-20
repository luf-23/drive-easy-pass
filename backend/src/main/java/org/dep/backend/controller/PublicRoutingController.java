package org.dep.backend.controller;

import org.dep.backend.dto.DrivingRouteResponse;
import org.dep.backend.service.AmapRoutingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/public/routing")
public class PublicRoutingController {
    private final AmapRoutingService amapRoutingService;

    public PublicRoutingController(AmapRoutingService amapRoutingService) {
        this.amapRoutingService = amapRoutingService;
    }

    /**
     * 驾车路径规划（沿道路），坐标为 WGS84，服务端会按高德要求使用 GCJ-02 请求。
     */
    @GetMapping("/driving")
    public DrivingRouteResponse driving(
            @RequestParam double originLng,
            @RequestParam double originLat,
            @RequestParam double destLng,
            @RequestParam double destLat,
            @RequestParam(required = false) String waypoints
    ) {
        try {
            return amapRoutingService.driving(
                    originLng,
                    originLat,
                    destLng,
                    destLat,
                    parseWaypoints(waypoints)
            );
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    /** waypoints=lng,lat;lng,lat（WGS84） */
    private static java.util.List<double[]> parseWaypoints(String raw) {
        if (raw == null || raw.isBlank()) {
            return java.util.List.of();
        }
        java.util.List<double[]> list = new java.util.ArrayList<>();
        for (String pair : raw.split(";")) {
            if (pair.isBlank()) {
                continue;
            }
            String[] parts = pair.split(",");
            if (parts.length < 2) {
                continue;
            }
            list.add(new double[] {
                    Double.parseDouble(parts[0].trim()),
                    Double.parseDouble(parts[1].trim())
            });
        }
        return list;
    }

    @GetMapping("/status")
    public RoutingStatus status() {
        return new RoutingStatus(amapRoutingService.isConfigured(), "amap-driving");
    }

    public record RoutingStatus(boolean amapConfigured, String mode) {}
}
