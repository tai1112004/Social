export const isUserOnline = (presence) => (
  presence?.status?.toLowerCase?.() === 'online'
);

const parsePresenceDate = (value) => {
  if (!value) return null;

  const normalized = String(value).includes('T')
    ? String(value)
    : String(value).replace(' ', 'T');
  const date = new Date(normalized);
  return Number.isNaN(date.getTime()) ? null : date;
};

export const getPresenceLabel = (presence) => {
  if (isUserOnline(presence)) return 'Đang hoạt động';

  const lastSeen = parsePresenceDate(presence?.lastSeenAt);
  if (!lastSeen) return 'Ngoại tuyến';

  return `Hoạt động lần cuối ${lastSeen.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })}`;
};
