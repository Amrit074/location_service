'use strict';
const $ = id => document.getElementById(id);
const ipv4 = /^(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}$/;
let activeRequest = null, lastResult = null, requesterIp = null;
const history = [];
function status(message, error = false) { $('status').textContent = message; $('status').dataset.error = String(error); }
async function request(url, options = {}) {
  const response = await fetch(url, options);
  let data;
  try { data = await response.json(); } catch { throw new Error('The service is starting or unavailable. Please try again shortly.'); }
  if (!response.ok) throw new Error(data.message || data.detail || `Lookup failed (${response.status}). Please try again.`);
  return data;
}
function clearResult() {
  lastResult = null; $('copy').disabled = true; $('copy').textContent = 'Copy result ↗';
  ['country', 'region', 'latitude', 'longitude'].forEach(id => $(id).textContent = '—');
  $('pin').hidden = true; $('map-link').hidden = true; $('map-empty').hidden = false;
}
function showResult(data, ip, duration) {
  lastResult = {...data, ipAddress: data.ipAddress || ip};
  $('result-ip').textContent = lastResult.ipAddress;
  $('place').textContent = data.city || data.state || data.country || 'Location details unavailable';
  $('country').textContent = data.country || 'Unavailable'; $('region').textContent = data.state || 'Unavailable';
  const lat = data.latitude, lon = data.longitude;
  const coordinates = typeof lat === 'number' && Number.isFinite(lat) && Math.abs(lat) <= 90 && typeof lon === 'number' && Number.isFinite(lon) && Math.abs(lon) <= 180;
  $('latitude').textContent = coordinates ? `${Math.abs(lat).toFixed(4)}° ${lat < 0 ? 'S' : 'N'}` : 'Unavailable';
  $('longitude').textContent = coordinates ? `${Math.abs(lon).toFixed(4)}° ${lon < 0 ? 'W' : 'E'}` : 'Unavailable';
  $('result-message').textContent = data.message || 'Approximate location returned by the geolocation service.';
  $('pin').hidden = !coordinates; $('map-empty').hidden = coordinates; $('map-link').hidden = !coordinates;
  if (coordinates) {
    $('pin').style.left = `${(lon + 180) / 360 * 100}%`; $('pin').style.top = `${(90 - lat) / 180 * 100}%`;
    $('pin-label').textContent = `${lat.toFixed(2)}°, ${lon.toFixed(2)}°`;
    $('map-link').href = `https://www.openstreetmap.org/?mlat=${lat}&mlon=${lon}#map=9/${lat}/${lon}`;
  }
  $('copy').disabled = false; status(`Lookup complete · ${duration} ms`);
}
function renderHistory() {
  $('history').replaceChildren(); $('count').textContent = String(history.length).padStart(2, '0'); $('clear').hidden = !history.length;
  if (!history.length) { const p = document.createElement('p'); p.className = 'history-empty'; p.textContent = 'Your successful lookups appear here for this tab. Nothing is saved after you leave.'; $('history').append(p); }
  history.forEach(item => {
    const button = document.createElement('button'); button.type = 'button'; button.className = 'history-item';
    const ip = document.createElement('strong'); ip.textContent = item.ip;
    const location = document.createElement('span'); location.textContent = [item.data.city, item.data.country].filter(Boolean).join(', ') || 'Location unavailable';
    const arrow = document.createElement('span'); arrow.textContent = 'Look up again ↗';
    button.append(ip, location, arrow); button.addEventListener('click', () => { $('ip').value = item.ip; lookup(item.ip); }); $('history').append(button);
  });
}
async function lookup(ip) {
  activeRequest?.abort(); activeRequest = null; clearResult();
  if (!ipv4.test(ip)) { $('place').textContent = 'Check the address.'; $('result-message').textContent = 'Enter a valid IPv4 address, such as 8.8.8.8.'; $('result-ip').textContent = 'INVALID ADDRESS'; status('Invalid IPv4 address', true); $('ip').focus(); $('search').disabled = false; $('results').setAttribute('aria-busy', 'false'); return; }
  const controller = new AbortController(); activeRequest = controller;
  const timeout = setTimeout(() => controller.abort(), 90000); const start = performance.now();
  $('search').disabled = true; $('results').setAttribute('aria-busy', 'true'); status('Looking up address…');
  $('result-ip').textContent = ip; $('place').textContent = 'Finding your point…'; $('result-message').textContent = 'The service may take a minute to wake up after inactivity.';
  try {
    const data = await request('/api/location/ip', {method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({ip}),signal:controller.signal});
    if (activeRequest !== controller) return;
    showResult(data, ip, Math.round(performance.now() - start));
    const previous = history.findIndex(item => item.ip === ip); if (previous >= 0) history.splice(previous, 1);
    history.unshift({ip, data}); history.splice(6); renderHistory();
  } catch (error) {
    if (activeRequest !== controller) return;
    status('Lookup unavailable', true); $('place').textContent = 'No result this time.';
    $('result-message').textContent = error.name === 'AbortError' ? 'The request timed out. Please try again when the service is ready.' : error.message;
  } finally { clearTimeout(timeout); if (activeRequest === controller) { activeRequest = null; $('search').disabled = false; $('results').setAttribute('aria-busy', 'false'); } }
}
$('lookup').addEventListener('submit', event => { event.preventDefault(); lookup($('ip').value.trim()); });
document.querySelectorAll('[data-ip]').forEach(button => button.addEventListener('click', () => { $('ip').value = button.dataset.ip; lookup(button.dataset.ip); }));
$('use-ip').addEventListener('click', () => { if (requesterIp) { $('ip').value = requesterIp; lookup(requesterIp); } });
$('clear').addEventListener('click', () => { history.length = 0; renderHistory(); });
$('copy').addEventListener('click', async () => { if (!lastResult) return; try { await navigator.clipboard.writeText(JSON.stringify(lastResult, null, 2)); $('copy').textContent = 'Copied ✓'; } catch { status('Clipboard unavailable', true); } });
(async () => { const controller = new AbortController(); const timeout = setTimeout(() => controller.abort(), 90000); try { const data = await request('/api/location/me', {signal:controller.signal}); if (!ipv4.test(data.ip || '')) throw new Error(); requesterIp = data.ip; $('my-ip').textContent = requesterIp; $('use-ip').disabled = false; } catch { $('my-ip').textContent = 'Unavailable'; } finally { clearTimeout(timeout); } })();
