package com.steply.app.analysis

object PoseLandmarks {
    const val Nose = "nose"
    const val LeftShoulder = "left_shoulder"
    const val RightShoulder = "right_shoulder"
    const val LeftElbow = "left_elbow"
    const val RightElbow = "right_elbow"
    const val LeftWrist = "left_wrist"
    const val RightWrist = "right_wrist"
    const val LeftHip = "left_hip"
    const val RightHip = "right_hip"
    const val LeftKnee = "left_knee"
    const val RightKnee = "right_knee"
    const val LeftAnkle = "left_ankle"
    const val RightAnkle = "right_ankle"

    val MediaPipeNames = listOf(
        Nose,
        "left_eye_inner",
        "left_eye",
        "left_eye_outer",
        "right_eye_inner",
        "right_eye",
        "right_eye_outer",
        "left_ear",
        "right_ear",
        "mouth_left",
        "mouth_right",
        LeftShoulder,
        RightShoulder,
        LeftElbow,
        RightElbow,
        LeftWrist,
        RightWrist,
        "left_pinky",
        "right_pinky",
        "left_index",
        "right_index",
        "left_thumb",
        "right_thumb",
        LeftHip,
        RightHip,
        LeftKnee,
        RightKnee,
        LeftAnkle,
        RightAnkle,
        "left_heel",
        "right_heel",
        "left_foot_index",
        "right_foot_index",
    )
}
