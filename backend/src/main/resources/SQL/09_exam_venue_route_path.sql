-- 路线/位置数据（表结构见 02_schema.sql）
SET NAMES utf8mb4;

INSERT INTO exam_venues (id, name, address, contact_phone) VALUES
(4, '天府新区金龙科目二考场', '天府新区科学城中路 88 号', '028-66112233'),
(5, '锦江白桦科目三考场', '锦江区桦林街 36 号', '028-77889900'),
(6, '郫都犀浦综合驾考基地', '郫都区犀浦镇国宁西路 200 号', '028-88776655')
ON DUPLICATE KEY UPDATE name = VALUES(name), address = VALUES(address), contact_phone = VALUES(contact_phone);

INSERT INTO exam_venue_routes (venue_id, exam_type, media_type, title, route_url, route_path, remark, enabled) VALUES
(2, '科目二', 'map', '宏远科目二考场', '',
 '{"routing":"venue","start":{"lat":30.5938,"lng":104.1748,"label":"宏远科目二电子化考场"},"end":{"lat":30.5938,"lng":104.1748,"label":"宏远科目二电子化考场"},"path":[[30.5938,104.1748]]}',
 '封闭式场内考试，地图展示考场位置', 1),
(3, '科目三', 'map', '顺达科目三道路驾驶路线', '',
 '{"routing":"driving","start":{"lat":30.6255,"lng":104.055,"label":"考场大门（起点）"},"end":{"lat":30.632,"lng":104.0685,"label":"考试终点"},"path":[[30.6255,104.055],[30.632,104.0685]]}',
 '社会化考场道路技能考试路线', 1),
(4, '科目二', 'map', '天府新区金龙科目二考场', '',
 '{"routing":"venue","start":{"lat":30.4025,"lng":104.0823,"label":"金龙科目二考场"},"end":{"lat":30.4025,"lng":104.0823,"label":"金龙科目二考场"},"path":[[30.4025,104.0823]]}',
 '天府新区科苑路附近训练场', 1),
(5, '科目三', 'map', '锦江白桦科目三路线', '',
 '{"routing":"driving","start":{"lat":30.6128,"lng":104.1186,"label":"白桦考场入口"},"end":{"lat":30.6215,"lng":104.1328,"label":"考试终点"},"path":[[30.6128,104.1186],[30.6215,104.1328]]}',
 '锦江城区道路驾驶考试', 1),
(6, '科目二', 'map', '郫都犀浦科目二考场', '',
 '{"routing":"venue","start":{"lat":30.7582,"lng":103.9786,"label":"犀浦综合基地科目二场"},"end":{"lat":30.7582,"lng":103.9786,"label":"犀浦综合基地科目二场"},"path":[[30.7582,103.9786]]}',
 '郫都犀浦国宁西路驾考基地', 1),
(6, '科目三', 'map', '郫都犀浦科目三路线', '',
 '{"routing":"driving","start":{"lat":30.7565,"lng":103.9720,"label":"基地出口（起点）"},"end":{"lat":30.7688,"lng":104.0055,"label":"考试终点"},"path":[[30.7565,103.972],[30.7688,104.0055]]}',
 '犀浦片区社会道路技能考试', 1)
ON DUPLICATE KEY UPDATE
  media_type = VALUES(media_type),
  title = VALUES(title),
  route_url = VALUES(route_url),
  route_path = VALUES(route_path),
  remark = VALUES(remark),
  enabled = VALUES(enabled);
