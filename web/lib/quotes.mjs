/** Curated, original bilingual reflections. No historical person attribution. */
export const moodCategory = Object.freeze({calm:'peace',positive:'gratitude',focused:'growth',refreshed:'rest'});
export function quoteForDay(quotes, dayKey) {
  if (!Array.isArray(quotes) || !quotes.length) throw new Error('Empty quote library');
  return quotes[((dayNumber(dayKey)%quotes.length)+quotes.length)%quotes.length];
}
export function quoteForMood(quotes, dayKey, mood) {
  const category = moodCategory[mood] || 'peace';
  const choices = quotes.filter(q => q.category === category);
  if (!choices.length) return quoteForDay(quotes, dayKey);
  return choices[(dayNumber(dayKey) + {peace:0,gratitude:3,growth:7,rest:11}[category]) % choices.length];
}
export function dayNumber(key) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(key)) throw new Error('Invalid day');
  const [year,month,day] = key.split('-').map(Number);
  const timestamp = Date.UTC(year,month-1,day);
  const date = new Date(timestamp);
  if(date.getUTCFullYear()!==year||date.getUTCMonth()!==month-1||date.getUTCDate()!==day)throw new Error('Invalid day');
  return Math.floor(timestamp/86400000);
}
