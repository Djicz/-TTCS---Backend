document.addEventListener("DOMContentLoaded", () => {
    // ── MANUAL SCROLL RESTORATION ──
    if ('scrollRestoration' in history) {
        history.scrollRestoration = 'manual';
    }

    // ── PROGRESS DATA ──
    const progressDataEl = document.getElementById('progress-data');
    if (!progressDataEl) {
        console.error("Reader: #progress-data element not found!");
        return;
    }

    const storyId = parseInt(progressDataEl.getAttribute('data-story-id'));
    const chapterId = parseInt(progressDataEl.getAttribute('data-chapter-id'));
    const savedPercentage = parseFloat(progressDataEl.getAttribute('data-scroll-percentage') || "0");

    console.log(`[Reader] Loaded Chapter ${chapterId} of Story ${storyId}. Saved progress: ${savedPercentage}%`);

    let isRestoring = savedPercentage > 0;
    let syncEnabled = false;

    // ── RESTORE SCROLL POSITION ──
    const restoreScroll = () => {
        if (!isRestoring) {
            console.log("[Reader] No saved progress to restore or already at 0%.");
            syncEnabled = true;
            return;
        }

        // Try restoring multiple times if necessary (for dynamic content)
        let attempts = 0;
        const maxAttempts = 5;

        const attemptRestore = () => {
            const scrollHeight = document.documentElement.scrollHeight - document.documentElement.clientHeight;
            if (scrollHeight > 50) { // Ensure there is actual content to scroll
                const scrollToY = (savedPercentage / 100) * scrollHeight;
                window.scrollTo(0, scrollToY);
                console.log(`[Reader] Restored scroll to ${savedPercentage}% (${scrollToY}px). scrollHeight=${scrollHeight}`);

                // Wait a bit before enabling sync to avoid overwriting with intermediate scroll values
                setTimeout(() => {
                    syncEnabled = true;
                    isRestoring = false;
                }, 1000);
            } else if (attempts < maxAttempts) {
                attempts++;
                console.log(`[Reader] Scroll height not ready (attempt ${attempts}/${maxAttempts}), retrying in 300ms...`);
                setTimeout(attemptRestore, 300);
            } else {
                console.warn("[Reader] Failed to restore scroll after max attempts. scrollHeight remains 0.");
                syncEnabled = true;
                isRestoring = false;
            }
        };

        // Give it a moment for images/rendering
        setTimeout(attemptRestore, 500);
    };

    if (document.readyState === 'complete') {
        restoreScroll();
    } else {
        window.addEventListener('load', restoreScroll);
    }

    // ── SYNC PROGRESS ──
    const syncProgress = () => {
        if (!syncEnabled) return;

        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const scrollHeight = document.documentElement.scrollHeight - document.documentElement.clientHeight;

        if (scrollHeight <= 0) return;

        const scrollPercentage = Math.round((scrollTop / scrollHeight) * 100);

        // Prevent accidental 0% overwrite if we are near the top and just loaded
        if (scrollPercentage === 0 && scrollTop < 50 && isRestoring) {
            console.log("[Reader] Skipping sync: still near top and potentially restoring.");
            return;
        }

        console.log(`[Reader] Syncing: ${scrollPercentage}% (${scrollTop}/${scrollHeight})`);

        fetch('/api/progress/save', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                storyId: storyId,
                chapterId: chapterId,
                scrollPercentage: scrollPercentage
            })
        }).catch(err => console.error("[Reader] Sync failed:", err));
    };

    function throttle(func, delay) {
        let lastCall = 0;
        return function (...args) {
            const now = new Date().getTime();
            if (now - lastCall < delay) return;
            lastCall = now;
            return func(...args);
        };
    }

    const throttledSync = throttle(syncProgress, 4000);
    window.addEventListener('scroll', throttledSync, { passive: true });

    // Final sync when leaving the page
    window.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'hidden') {
            syncProgress();
        }
    });

    // ── STICKY TOPBAR ──
    let lastScrollY = window.scrollY;
    window.addEventListener('scroll', () => {
        const currentScrollY = window.scrollY;
        if (currentScrollY < lastScrollY || currentScrollY < 50) {
            document.body.classList.remove('header-hidden');
        } else if (currentScrollY > lastScrollY && currentScrollY > 100) {
            document.body.classList.add('header-hidden');
        }
        lastScrollY = currentScrollY;
    }, { passive: true });

    // ── VIEW COUNT & READING TIME ──
    setTimeout(() => {
        fetch(`/api/stories/${storyId}/view`, { method: 'POST' }).catch(() => { });
    }, 5000);

    let activeSeconds = 0;
    setInterval(() => {
        if (document.visibilityState === 'visible') {
            activeSeconds++;
            if (activeSeconds >= 30) {
                fetch(`/api/user/reading-time?seconds=${activeSeconds}`, { method: 'POST' })
                    .then(res => { if (res.ok) activeSeconds = 0; })
                    .catch(() => { });
            }
        }
    }, 1000);
});
