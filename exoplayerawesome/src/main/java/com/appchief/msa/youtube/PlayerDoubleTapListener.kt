package com.appchief.msa.youtube

interface PlayerDoubleTapListener {
    /**
     * Called when double tapping starts, after double tap gesture
     *
     * @param posX x tap position on the root view
     * @param posY y tap position on the root view
     */
    fun onDoubleTapStarted(posX: Float, posY: Float) {}

    /**
     * Called for each ongoing tap (also single tap) (MotionEvent#ACTION_DOWN)
     * when double tap started and still in double tap mode defined
     * by [DoubleTapPlayerView.doubleTapDelay]
     *
     * @param posX x tap position on the root view
     * @param posY y tap position on the root view
     */
    fun onDoubleTapProgressDown(posX: Float, posY: Float) {}

    /**
     * Called for each ongoing tap (also single tap) (MotionEvent#ACTION_UP}
     * when double tap started and still in double tap mode defined
     * by [DoubleTapPlayerView.doubleTapDelay]
     *
     * @param posX x tap position on the root view
     * @param posY y tap position on the root view
     */
    fun onDoubleTapProgressUp(posX: Float, posY: Float) {}

    /**
     * Called when [DoubleTapPlayerView.doubleTapDelay] is over
     */
    fun onDoubleTapFinished() {}
}