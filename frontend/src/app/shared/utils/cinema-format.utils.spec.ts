import { formatDate, formatTime, formatDuration } from './cinema-format.utils';

describe('formatDate', () => {
  it('returns empty string for undefined', () => {
    expect(formatDate(undefined)).toBe('');
  });

  it('formats a date string to short locale date', () => {
    const result = formatDate('2026-06-15T20:00:00');
    expect(result).toContain('2026');
    expect(result).toContain('Jun');
    expect(result).toContain('15');
  });
});

describe('formatTime', () => {
  it('returns empty string for undefined', () => {
    expect(formatTime(undefined)).toBe('');
  });

  it('formats a date string to HH:mm', () => {
    // Use a fixed UTC time to avoid timezone variance across environments
    const result = formatTime('2026-06-15T00:00:00Z');
    expect(result).toMatch(/^\d{2}:\d{2}$/);
  });
});

describe('formatDuration', () => {
  it('formats minutes only', () => {
    expect(formatDuration(45)).toBe('45min');
  });

  it('formats hours only', () => {
    expect(formatDuration(120)).toBe('2h');
  });

  it('formats hours and minutes', () => {
    expect(formatDuration(95)).toBe('1h 35min');
  });

  it('formats zero minutes', () => {
    expect(formatDuration(0)).toBe('0min');
  });
});
