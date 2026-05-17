export type OptionKey = "A" | "B" | "C" | "D";

export interface Question {
  id: number;
  content: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  answer: OptionKey;
  explanation: string;
}

export interface WrongQuestion {
  id: number;
  userId: number;
  question: Question;
  createTime: string;
}

export interface ExamResult {
  total: number;
  correct: number;
  score: number;
  wrongQuestions: Question[];
}

export interface UserProfile {
  id: number;
  username: string;
  nickname: string;
  roles?: string[];
  permissions?: string[];
}

export interface AuthResponse {
  token: string;
  user: UserProfile;
}

export interface AppRoute {
  id: number;
  path: string;
  name: string;
  title: string;
  parentId: number | null;
  redirect: string;
  component: string;
  icon: string;
  rankNo: number;
  enabled: boolean;
}

export interface Role {
  id: number;
  code: string;
  name: string;
  description: string;
  enabled: boolean;
  routeIds: number[];
}

export interface PageResult<T> {
  items: T[];
  total: number;
}

export interface EnrollmentLead {
  id: number;
  name: string;
  phone: string;
  source: string;
  intentLevel: string;
  status: string;
  ownerUserId: number | null;
  ownerName: string | null;
  nextFollowTime: string | null;
  remark: string;
  createTime: string;
  updateTime: string;
}

export interface EnrollmentLeadPayload {
  name: string;
  phone: string;
  source: string;
  intentLevel: string;
  status: string;
  ownerUserId: number | null;
  nextFollowTime: string | null;
  remark: string;
}

export interface EnrollmentLeadQuery {
  keyword?: string;
  status?: string;
  source?: string;
  ownerUserId?: number | null;
  startDate?: string;
  endDate?: string;
  page?: number;
  pageSize?: number;
}

export interface EnrollmentFollowUp {
  id: number;
  leadId: number;
  content: string;
  followType: string;
  nextFollowTime: string | null;
  creatorUserId: number | null;
  creatorName: string | null;
  createTime: string;
}

export interface EnrollmentFollowUpPayload {
  content: string;
  followType: string;
  nextFollowTime: string | null;
}

export interface EnrollmentSourceStat {
  source: string;
  count: number;
}

export interface EnrollmentOwnerPerformance {
  ownerUserId: number | null;
  ownerName: string;
  signedCount: number;
}

export interface EnrollmentIntentStat {
  intentLevel: string;
  count: number;
}

export interface EnrollmentFunnelStat {
  stage: string;
  count: number;
}

export interface EnrollmentDashboard {
  todayNewLeads: number;
  monthConversionRate: number;
  sourceDistribution: EnrollmentSourceStat[];
  ownerRanking: EnrollmentOwnerPerformance[];
  intentDistribution: EnrollmentIntentStat[];
  funnel: EnrollmentFunnelStat[];
}

export interface PublicEnrollmentIntentPayload {
  name: string;
  phone: string;
  vehicleType: string;
  classType: string;
  source: string;
  remark: string;
}

export interface CoursePackage {
  code: string;
  name: string;
  price: number;
  lessonHours: number;
  highlights: string[];
  tag: string;
}

/** 可预约场次（车管所/考场排期） */
export interface ExamScheduleCard {
  id: number;
  venueId: number;
  venueName: string;
  examType: string;
  examDate: string;
  startTime: string;
  endTime: string;
  capacity: number;
  bookedCount: number;
  availableSlots: number;
  remark: string;
}

/** 学员本人报考记录 */
export interface ExamRegistrationRow {
  id: number;
  scheduleId: number;
  venueId: number;
  venueName: string;
  examType: string;
  examDate: string;
  startTime: string;
  endTime: string;
  status: string;
  score: number | null;
  passed: string | null;
  availableSlots: number;
}

/** 管理员报考列表一行 */
export interface ExamReservationAdminRow {
  id: number;
  userId: number;
  username: string;
  scheduleId: number;
  venueId: number;
  venueName: string;
  examType: string;
  examDate: string;
  startTime: string;
  status: string;
  score: number | null;
  passed: string | null;
  remark: string;
}

export interface AdminExamRegistrationUpdate {
  status: string | null;
  score: number | null;
  passed: string | null;
  remark: string | null;
}
