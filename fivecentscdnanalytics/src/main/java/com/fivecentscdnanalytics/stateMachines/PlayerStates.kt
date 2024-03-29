package com.fivecentscdnanalytics.stateMachines

import com.fivecentscdnanalytics.data.ErrorCode
import com.fivecentscdnanalytics.enums.AnalyticsErrorCodes


class PlayerStates {
    companion object {
        @JvmField val READY = DefaultPlayerState<Void>("ready")
        @JvmField val SOURCE_CHANGED = DefaultPlayerState<Void>("source_changed")
        @JvmField val STARTUP = object : DefaultPlayerState<Void>("startup") {
            override fun onEnterState(machine: PlayerStateMachine, data: Void?) {
                machine.videoStartTimeout.start()
            }

            override fun onExitState(
                machine: PlayerStateMachine,
                elapsedTime: Long,
                destinationPlayerState: PlayerState<*>
            ) {
                machine.videoStartTimeout.cancel()
                val elapsedTimeOnEnter = machine.elapsedTimeOnEnter
                machine.addStartupTime(elapsedTime - elapsedTimeOnEnter)
                if (destinationPlayerState === PLAYING) {
                    val playerStartupTime = machine.andResetPlayerStartupTime
                    for (listener in machine.listeners) {
                        listener.onStartup(machine.startupTime, playerStartupTime)
                    }
                    machine.isStartupFinished = true
                }
            }
        }
        @JvmField val AD = DefaultPlayerState<Void>("ad")
        @JvmField val ADFINISHED = DefaultPlayerState<Void>("adfinished")
        @JvmField val BUFFERING = object : DefaultPlayerState<Void>("buffering") {
            override fun onEnterState(machine: PlayerStateMachine, data: Void?) {
                machine.enableRebufferHeartbeat()
                machine.rebufferingTimeout.start()
            }

            override fun onExitState(
                machine: PlayerStateMachine,
                elapsedTime: Long,
                destinationPlayerState: PlayerState<*>
            ) {
                machine.disableRebufferHeartbeat()
                for (listener in machine.listeners) {
                    val elapsedTimeOnEnter = machine.elapsedTimeOnEnter
                    listener.onRebuffering(elapsedTime - elapsedTimeOnEnter)
                }
                machine.rebufferingTimeout.cancel()
            }
        }
        @JvmField val ERROR = object : DefaultPlayerState<ErrorCode>("error") {
            override fun onEnterState(machine: PlayerStateMachine, data: ErrorCode?) {
                machine.videoStartTimeout.cancel()
                for (listener in machine.listeners) {
                    listener.onError(data)
                }
            }

            override fun onExitState(
                machine: PlayerStateMachine,
                elapsedTime: Long,
                destinationPlayerState: PlayerState<*>
            ) {
                machine.videoStartFailedReason = null
            }
        }
        @JvmField val EXITBEFOREVIDEOSTART = object : DefaultPlayerState<Void>("exitbeforevideostart") {
            override fun onEnterState(machine: PlayerStateMachine, data: Void?) {
                for (listener in machine.listeners) {
                    listener.onVideoStartFailed()
                }
            }

            override fun onExitState(
                machine: PlayerStateMachine,
                elapsedTime: Long,
                destinationPlayerState: PlayerState<*>
            ) {
                machine.videoStartFailedReason = null
            }
        }
        @JvmField val PLAYING = object : DefaultPlayerState<Void>("playing") {
            override fun onEnterState(machine: PlayerStateMachine, data: Void?) {
                machine.enableHeartbeat()
            }

            override fun onExitState(
                machine: PlayerStateMachine,
                elapsedTime: Long,
                destinationPlayerState: PlayerState<*>
            ) {
                for (listener in machine.listeners) {
                    val elapsedTimeOnEnter = machine.elapsedTimeOnEnter
                    listener.onPlayExit(elapsedTime - elapsedTimeOnEnter)
                }
                machine.disableHeartbeat()
            }
        }
        @JvmField val PAUSE = object : DefaultPlayerState<Void>("pause") {
            override fun onExitState(
                machine: PlayerStateMachine,
                elapsedTime: Long,
                destinationPlayerState: PlayerState<*>
            ) {
                for (listener in machine.listeners) {
                    val elapsedTimeOnEnter = machine.elapsedTimeOnEnter
                    listener.onPauseExit(elapsedTime - elapsedTimeOnEnter)
                }
            }
        }
        @JvmField val QUALITYCHANGE = object : DefaultPlayerState<Void>("qualitychange") {
            override fun onEnterState(machine: PlayerStateMachine, data: Void?) {
                machine.increaseQualityChangeCount()
                if (!machine.isQualityChangeTimerRunning) {
                    machine.qualityChangeResetTimeout.start()
                }
            }

            override fun onExitState(
                machine: PlayerStateMachine,
                elapsedTime: Long,
                destinationPlayerState: PlayerState<*>
            ) {
                if (machine.isQualityChangeEventEnabled) {
                    for (listener in machine.listeners) {
                        listener.onQualityChange()
                    }
                } else {
                    val errorCode = AnalyticsErrorCodes.ANALYTICS_QUALITY_CHANGE_THRESHOLD_EXCEEDED
                        .errorCode
                    for (listener in machine.listeners) {
                        listener.onError(errorCode)
                    }
                }
            }
        }
        @JvmField val CUSTOMDATACHANGE = DefaultPlayerState<Void>("customdatachange")
        @JvmField val AUDIOTRACKCHANGE = object : DefaultPlayerState<Void>("audiotrackchange") {
            override fun onExitState(
                machine: PlayerStateMachine,
                elapsedTime: Long,
                destinationPlayerState: PlayerState<*>
            ) {
                for (listener in machine.listeners) {
                    listener.onAudioTrackChange()
                }
            }
        }
        @JvmField val SUBTITLECHANGE = object : DefaultPlayerState<Void>("subtitlechange") {
            override fun onExitState(
                machine: PlayerStateMachine,
                elapsedTime: Long,
                destinationPlayerState: PlayerState<*>
            ) {
                for (listener in machine.listeners) {
                    listener.onSubtitleChange()
                }
            }
        }

        @JvmField val SEEKING = object : DefaultPlayerState<Void>("seeking") {
            override fun onEnterState(machine: PlayerStateMachine, data: Void?) {
                machine.elapsedTimeSeekStart = machine.elapsedTimeOnEnter
            }

            override fun onExitState(
                machine: PlayerStateMachine,
                elapsedTime: Long,
                destinationPlayerState: PlayerState<*>
            ) {
                for (listener in machine.listeners) {
                    listener.onSeekComplete(elapsedTime - machine.elapsedTimeSeekStart)
                }
                machine.elapsedTimeSeekStart = 0
            }
        }
    }
}
