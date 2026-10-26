export interface MindQuote { id:number;category:string;en:string;mm:string }
export type Mood = 'calm'|'positive'|'focused'|'refreshed';
export const moodCategory: Readonly<Record<Mood,string>>;
export function dayNumber(key:string):number;
export function quoteForDay(quotes:MindQuote[],dayKey:string):MindQuote;
export function quoteForMood(quotes:MindQuote[],dayKey:string,mood:Mood):MindQuote;
