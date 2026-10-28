// Shows the signed-in admin and a "Sign out" button in every admin page's navbar.
// The pages themselves are protected server-side; this is only the UI.
(() => {
    async function signOut() {
        try { await fetch('/api/auth/logout', { method: 'POST' }); } finally { location.replace('/admin/login'); }
    }

    async function init() {
        let me = null;
        try {
            const res = await fetch('/api/auth/me');
            if (res.ok) me = await res.json();
        } catch { /* offline: leave the navbar alone */ }
        if (!me || me.role !== 'ROLE_ADMIN') { location.replace('/admin/login?next=' + encodeURIComponent(location.pathname)); return; }

        const storeLink = [...document.querySelectorAll('nav a')].find(a => /Back to Store/i.test(a.textContent));
        const host = storeLink?.parentElement || document.querySelector('nav .container-fluid, nav .container, nav');
        if (!host) return;

        const wrap = document.createElement('div');
        wrap.className = 'd-flex align-items-center gap-2 ms-2';
        const who = document.createElement('span');
        who.className = 'text-white-50 small d-none d-md-inline';
        who.textContent = `Signed in as ${me.firstName || me.username}`;
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'btn btn-outline-light btn-sm';
        btn.innerHTML = '<i class="bi bi-box-arrow-right"></i> Sign out';
        btn.addEventListener('click', signOut);
        wrap.append(who, btn);
        host.classList.add('d-flex', 'align-items-center');
        host.appendChild(wrap);
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
    else init();
})();
