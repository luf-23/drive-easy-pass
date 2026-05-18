import type { OptionKey, Question } from "../types";

const JUDGMENT_LABELS = new Set(["正确", "错误", "对", "错", "是", "否"]);

export function optionText(question: Question, key: OptionKey): string {
  return (question[`option${key}` as keyof Question] as string)?.trim() ?? "";
}

export function optionKeysFor(question: Question): OptionKey[] {
  return (["A", "B", "C", "D"] as OptionKey[]).filter(key =>
    optionText(question, key)
  );
}

/** 仅 A/B 两选项且文案为判断题常用词（正确/错误等） */
export function isJudgmentQuestion(question: Question): boolean {
  const keys = optionKeysFor(question);
  if (keys.length !== 2 || keys[0] !== "A" || keys[1] !== "B") {
    return false;
  }
  return keys.every(key => JUDGMENT_LABELS.has(optionText(question, key)));
}

export function judgmentVariant(
  question: Question,
  key: OptionKey
): "true" | "false" | null {
  const text = optionText(question, key);
  if (text === "正确" || text === "对" || text === "是") return "true";
  if (text === "错误" || text === "错" || text === "否") return "false";
  return null;
}

export function judgmentIcon(question: Question, key: OptionKey): string {
  const variant = judgmentVariant(question, key);
  if (variant === "true") return "✓";
  if (variant === "false") return "✗";
  return key;
}
