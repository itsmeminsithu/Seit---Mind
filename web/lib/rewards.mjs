/** Local-only reward calculations. Do not use as an authoritative server ledger. */
export const DAILY_STAR_CAP = 60;
export const COMPLETION_FRACTION = 0.8;
export function localDateKey(date) {
  const z = n => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${z(date.getMonth() + 1)}-${z(date.getDate())}`;
}
export function previousLocalDateKey(date) {
  const d = new Date(date.getFullYear(), date.getMonth(), date.getDate() - 1);
  return localDateKey(d);
}
export function calculateCompletion(previous, elapsedSeconds, plannedMinutes, now = new Date()) {
  if (!Number.isInteger(plannedMinutes) || plannedMinutes < 1 || plannedMinutes > 180 ||
      !Number.isFinite(elapsedSeconds) || elapsedSeconds < 0) return null;
  const plannedSeconds = plannedMinutes * 60;
  if (elapsedSeconds + 0.001 < plannedSeconds * COMPLETION_FRACTION) return null;
  const minutes = Math.floor(Math.min(elapsedSeconds, plannedSeconds) / 60);
  const date = localDateKey(now);
  const lastCompleted = typeof previous.lastCompleted === 'string' ? previous.lastCompleted : '';
  const priorStreak = Math.max(0, Number.isInteger(previous.streak) ? previous.streak : 0);
  const streak = lastCompleted === date ? Math.max(1, priorStreak) :
    lastCompleted === previousLocalDateKey(now) ? priorStreak + 1 : 1;
  const dailyUsed = previous.dailyStarsDate === date ? Math.min(DAILY_STAR_CAP, Math.max(0, previous.dailyStars || 0)) : 0;
  const earned = Math.max(0, Math.min(DAILY_STAR_CAP - dailyUsed, minutes));
  const previousStars = Math.max(0, Number.isFinite(previous.stars) ? previous.stars : 0);
  return {
    next: { ...previous, streak, lastCompleted: date, stars: previousStars + earned,
      dailyStars: dailyUsed + earned, dailyStarsDate: date },
    session: { date, minutes, streak, stars: previousStars + earned, earned }
  };
}
