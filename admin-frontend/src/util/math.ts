export function min<T>(values: Array<T>): T | null {
  if (values.length == null) {
    return null;
  }
  let min = values[0];
  for (let i = 1; i < values.length; i += 1) {
    if (values[i] < min) {
      min = values[i];
    }
  }
  return min;
}

export function max<T>(values: Array<T>): T | null {
  if (values.length == null) {
    return null;
  }
  let max = values[0];
  for (let i = 1; i < values.length; i += 1) {
    if (values[i] > max) {
      max = values[i];
    }
  }
  return max;
}
