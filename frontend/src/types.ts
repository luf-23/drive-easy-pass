export type OptionKey = 'A' | 'B' | 'C' | 'D'

export interface Question {
  id: number
  content: string
  optionA: string
  optionB: string
  optionC: string
  optionD: string
  answer: OptionKey
  explanation: string
}

export interface WrongQuestion {
  id: number
  userId: number
  question: Question
  createTime: string
}

export interface ExamResult {
  total: number
  correct: number
  score: number
  wrongQuestions: Question[]
}

export interface UserProfile {
  id: number
  username: string
  nickname: string
  roles?: string[]
  permissions?: string[]
}

export interface AuthResponse {
  token: string
  user: UserProfile
}

export interface AppRoute {
  id: number
  path: string
  name: string
  title: string
  parentId: number | null
  component: string
  icon: string
  rankNo: number
  enabled: boolean
}

export interface Role {
  id: number
  code: string
  name: string
  description: string
  enabled: boolean
  routeIds: number[]
}

export interface PageResult<T> {
  items: T[]
  total: number
}

export interface EnrollmentLead {
  id: number
  name: string
  phone: string
  source: string
  intentLevel: string
  status: string
  ownerUserId: number | null
  ownerName: string | null
  nextFollowTime: string | null
  remark: string
  createTime: string
  updateTime: string
}

export interface EnrollmentLeadPayload {
  name: string
  phone: string
  source: string
  intentLevel: string
  status: string
  ownerUserId: number | null
  nextFollowTime: string | null
  remark: string
}

export interface EnrollmentLeadQuery {
  keyword?: string
  status?: string
  source?: string
  ownerUserId?: number | null
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}

export interface EnrollmentFollowUp {
  id: number
  leadId: number
  content: string
  followType: string
  nextFollowTime: string | null
  creatorUserId: number | null
  creatorName: string | null
  createTime: string
}

export interface EnrollmentFollowUpPayload {
  content: string
  followType: string
  nextFollowTime: string | null
}

export interface EnrollmentSourceStat {
  source: string
  count: number
}

export interface EnrollmentOwnerPerformance {
  ownerUserId: number | null
  ownerName: string
  signedCount: number
}

export interface EnrollmentIntentStat {
  intentLevel: string
  count: number
}

export interface EnrollmentFunnelStat {
  stage: string
  count: number
}

export interface EnrollmentDashboard {
  todayNewLeads: number
  monthConversionRate: number
  sourceDistribution: EnrollmentSourceStat[]
  ownerRanking: EnrollmentOwnerPerformance[]
  intentDistribution: EnrollmentIntentStat[]
  funnel: EnrollmentFunnelStat[]
}

export interface PublicEnrollmentIntentPayload {
  name: string
  phone: string
  vehicleType: string
  classType: string
  source: string
  remark: string
}

export interface CoursePackage {
  code: string
  name: string
  price: number
  lessonHours: number
  highlights: string[]
  tag: string
}

export interface ExamSite {
  code: string
  name: string
  address: string
  subjects: string[]
  imageUrl: string
  routeDescription: string
}
