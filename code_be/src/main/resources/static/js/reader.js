document.addEventListener("DOMContentLoaded", () => {
    // ── SCROLL HIDE/SHOW TOPBAR (Sticky behavior) ──
    let lastScrollY = window.scrollY;
    window.addEventListener('scroll', () => {
        const currentScrollY = window.scrollY;
        // Show if at top or scrolling up, hide if scrolling down and not at top
        if (currentScrollY < lastScrollY || currentScrollY < 50) {
            document.body.classList.remove('header-hidden');
        } else if (currentScrollY > lastScrollY && currentScrollY > 100) {
            document.body.classList.add('header-hidden');
        }
        lastScrollY = currentScrollY;
    }, { passive: true });

    // ── PROGRESS DATA ──
    const progressDataEl = document.getElementById('progress-data');
    if (!progressDataEl) return;
    const storyId = progressDataEl.getAttribute('data-story-id');
    const chapterId = progressDataEl.getAttribute('data-chapter-id');

    function throttle(mainFunction, delay) {
        let timerFlag = null;
        return (...args) => {
            if (timerFlag === null) {
                mainFunction(...args);
                timerFlag = setTimeout(() => {
                    timerFlag = null;
                }, delay);
            }
        };
    }

    const syncProgress = () => {
        const scrollTop = window.scrollY || document.documentElement.scrollTop;
        const scrollHeight = document.documentElement.scrollHeight - document.documentElement.clientHeight;
        const scrollPercentage = Math.round((scrollTop / scrollHeight) * 100);
        fetch('/api/progress/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                storyId: storyId,
                chapterId: chapterId,
                scrollPercentage: scrollPercentage
            })
        }).catch(err => console.debug("Sync failed", err));
    };

    const throttledSync = throttle(syncProgress, 5000);
    window.addEventListener('scroll', throttledSync, { passive: true });
    setTimeout(syncProgress, 2000); 

    setTimeout(() => {
        fetch(`/api/stories/${storyId}/view`, {
            method: 'POST'
        }).catch(err => console.debug("Increment view failed", err));
    }, 5000);

    let activeSeconds = 0;
    let visibilityCheckInterval = null;
    const pingReadingTime = () => {
        if (activeSeconds > 0) {
            const formData = new URLSearchParams();
            formData.append('seconds', Math.floor(activeSeconds));
            fetch(`/api/user/reading-time?seconds=${Math.floor(activeSeconds)}`, {
                method: 'POST'
            }).then(res => {
                if (res.ok) {
                    activeSeconds = 0; 
                }
            }).catch(console.debug);
        }
    };

    const startTrackingTime = () => {
        if (visibilityCheckInterval) clearInterval(visibilityCheckInterval);
        visibilityCheckInterval = setInterval(() => {
            if (document.visibilityState === 'visible') {
                activeSeconds++;
                if (activeSeconds >= 15) {
                    pingReadingTime();
                }
            }
        }, 1000);
    };

    const stopTrackingTime = () => {
        if (visibilityCheckInterval) {
            clearInterval(visibilityCheckInterval);
            visibilityCheckInterval = null;
        }
        pingReadingTime(); 
    };

    if (document.visibilityState === 'visible') {
        startTrackingTime();
    }
    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'visible') {
            startTrackingTime();
        } else {
            stopTrackingTime();
        }
    });

    window.addEventListener('beforeunload', () => {
        stopTrackingTime();
    });
});
