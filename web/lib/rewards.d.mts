export interface RewardProfileLike {
  stars: number;
  streak: number;
  lastCompleted: string;
  dailyStars: number;
  dailyStarsDate: string;
}
export interface RewardSession {
  date: string;
  minutes: number;
  streak: number;
  stars: number;
  earned: number;
}
export const DAILY_STAR_CAP: number;
export const COMPLETION_FRACTION: number;
export function localDateKey(date: Date): string;
export function previousLocalDateKey(date: Date): string;
export function calculateCompletion<T extends RewardProfileLike>(previous: T, elapsedSeconds: number, plannedMinutes: number, now?: Date): {next: T;session: RewardSession}|null;
