INSERT INTO questions (id, content, option_a, option_b, option_c, option_d, answer, explanation) VALUES
(1, 'What type of behavior is violating road traffic safety law while driving?', 'Minor mistake', 'Illegal behavior', 'Careless behavior', 'Common behavior', 'B', 'Violating road traffic safety law is illegal behavior.'),
(2, 'How long is the probation period after first getting a driving license?', '6 months', '12 months', '18 months', '24 months', 'B', 'The first 12 months after getting a driving license are the probation period.'),
(3, 'What should you do when vehicles ahead are waiting in line?', 'Cut in from both sides', 'Overtake from the right', 'Queue in order', 'Honk to hurry them', 'C', 'When vehicles are waiting in line, you should queue in order.'),
(4, 'How should lights be used at night when passing an uncontrolled intersection?', 'Turn off high beam', 'Use hazard lights', 'Flash high and low beams alternately', 'Keep high beam on', 'C', 'At night, flash high and low beams alternately to warn others at an uncontrolled intersection.'),
(5, 'On an expressway, how far behind the vehicle should the warning sign be placed after a breakdown?', '50 meters', '100 meters', '150 meters', '200 meters', 'C', 'On an expressway, place the warning sign at least 150 meters behind the vehicle.'),
(6, 'What should you do when pedestrians are crossing a crosswalk?', 'Slow down and honk', 'Stop and yield', 'Speed up', 'Pass in front of them', 'B', 'When pedestrians are crossing a crosswalk, vehicles must stop and yield.'),
(7, 'What may happen when braking on a rainy day?', 'Engine stalls', 'Steering wheel locks', 'Skidding or longer braking distance', 'Lower fuel use', 'C', 'Wet roads reduce grip and may cause skidding or longer braking distance.'),
(8, 'How many days before expiry may you apply to renew a driving license?', '30 days', '60 days', '90 days', '6 months', 'C', 'A driving license can be renewed within 90 days before expiry.'),
(9, 'Temporary parking on the road must not obstruct what?', 'Other vehicles and pedestrians', 'Passengers resting', 'Items inside the car', 'Checking mirrors', 'A', 'Temporary parking must not obstruct other vehicles and pedestrians.'),
(10, 'What is correct when driving down a steep slope?', 'Coast in neutral', 'Turn off engine and coast', 'Use engine braking to control speed', 'Only use parking brake', 'C', 'Use a low gear and engine braking on steep downhill roads.'),
(11, 'What should following vehicles do when a school bus stops with hazard lights on for students?', 'Honk', 'Stop and wait', 'Pass quickly on the left', 'Pass slowly on the right', 'B', 'Vehicles behind a school bus should stop and wait when students get on or off.'),
(12, 'Before changing lanes, which light should be turned on in advance?', 'Hazard lights', 'Turn signal', 'High beam', 'Fog light', 'B', 'Before changing lanes, turn on the turn signal and confirm safety.')
ON DUPLICATE KEY UPDATE
  content = VALUES(content),
  option_a = VALUES(option_a),
  option_b = VALUES(option_b),
  option_c = VALUES(option_c),
  option_d = VALUES(option_d),
  answer = VALUES(answer),
  explanation = VALUES(explanation);
