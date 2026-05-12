package com.srll.gamification.service;

import com.srll.gamification.document.UserProgress;

public interface BadgeObserver {
    void onProgressUpdated(UserProgress progress, long streakDays);
}
