export interface ExamVenueDTO {
  id: number
  venueName: string
  venueCode: string
  address: string
  district: string
  contactPhone: string | null
  examType: string
  totalSlots: number
  availableSlots: number
  routeDescription: string | null
  routeMapUrl: string | null
  facilities: string | null
  businessHours: string | null
  longitude: number | null
  latitude: number | null
  status: string
}
export interface ExamVenueDTO {
  id: number
  venueName: string
  venueCode: string
  address: string
  district: string
  contactPhone: string | null
  examType: string
  totalSlots: number
  availableSlots: number
  routeDescription: string | null
  routeMapUrl: string | null
  facilities: string | null
  businessHours: string | null
  longitude: number | null
  latitude: number | null
  status: string
}

export interface ExamRouteDTO {
  id: number
  venueId: number
  routeName: string
  routeNumber: string
  description: string | null
  startPoint: string | null
  endPoint: string | null
  distance: number | null
  difficulty: string | null
  points: string | null
  mapImageUrl: string | null
  sortOrder: number
}

export interface ExamScheduleDTO {
  id: number
  venueId: number
  venueName: string
  examDate: string
  startTime: string
  endTime: string
  examType: string
  totalSlots: number
  reservedSlots: number
  availableSlots: number
  status: string
}

export interface ExamReservationDTO {
  id: number
  userId: number
  username: string
  scheduleId: number
  venueId: number
  venueName: string
  examType: string
  examDate: string
  startTime: string
  status: string
  score: number | null
  passed: string | null
  remark: string | null
}
export interface ExamRouteDTO {
  id: number
  venueId: number
  routeName: string
  routeNumber: string
  description: string | null
  startPoint: string | null
  endPoint: string | null
  distance: number | null
  difficulty: string | null
  points: string | null
  mapImageUrl: string | null
  sortOrder: number
}

export interface ExamScheduleDTO {
  id: number
  venueId: number
  venueName: string
  examDate: string
  startTime: string
  endTime: string
  examType: string
  totalSlots: number
  reservedSlots: number
  availableSlots: number
  status: string
}
