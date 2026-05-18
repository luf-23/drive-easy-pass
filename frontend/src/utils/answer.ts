import type { OptionKey } from "../types";

export function normalizeAnswer(answer: string): string {
  return answer
    .toUpperCase()
    .replace(/[^A-D]/g, "")
    .split("")
    .sort()
    .join("");
}

export function isMultiAnswer(answer: string): boolean {
  return normalizeAnswer(answer).length > 1;
}

export function isAnswerCorrect(selected: string, expected: string): boolean {
  return normalizeAnswer(selected) === normalizeAnswer(expected);
}

export function toggleAnswer(selected: string, key: OptionKey): string {
  const letters = normalizeAnswer(selected).split("");
  const index = letters.indexOf(key);
  if (index >= 0) {
    letters.splice(index, 1);
  } else {
    letters.push(key);
  }
  return letters.sort().join("");
}

export function formatAnswer(answer: string): string {
  const normalized = normalizeAnswer(answer);
  return normalized.split("").join("、");
}
