export function formatDate(date: Date): string {
    return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일 ${padLeft(String(date.getHours()), '0', 2)}:${padLeft(String(date.getMinutes()), '0', 2)}`;
}

function padLeft(value: string, padValue: string, targetLength: number): string {
    if (value.length >= targetLength) {
        return value;
    }
    let result = '';
    for (let i = 0; i < targetLength - value.length; i++) {
        result += padValue;
    }
    result += value;
    return result;
}
