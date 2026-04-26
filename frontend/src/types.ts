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
