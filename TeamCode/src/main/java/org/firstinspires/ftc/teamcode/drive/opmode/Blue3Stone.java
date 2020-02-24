package org.firstinspires.ftc.teamcode.drive.opmode;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.AutoBackend.CustomSkystoneDetector;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;

import org.firstinspires.ftc.teamcode.OmegaBotRR;
import org.firstinspires.ftc.teamcode.drive.mecanum.SampleMecanumDriveBase;
import org.firstinspires.ftc.teamcode.drive.mecanum.SampleMecanumDriveREV;
import org.firstinspires.ftc.teamcode.drive.mecanum.SampleMecanumDriveREVOptimized;


import kotlin.Unit;

@Autonomous(group = "drive")
public class Blue3Stone extends LinearOpMode {
    OmegaBotRR robot;
    private OpenCvCamera phoneCam;
    private CustomSkystoneDetector skyStoneDetector;

    String skystonePosition = "none";
    double xPosition;
    double yPosition;

    Pose2d ROBOT_INIT_POSITION = new Pose2d(-39,-63,0);

    @Override
    public void runOpMode() throws InterruptedException {
        // initialize robot and drivetrain
        robot = new OmegaBotRR(telemetry, hardwareMap);
        SampleMecanumDriveBase drive = new SampleMecanumDriveREV(hardwareMap);
        //initializes camera detection stuff
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        phoneCam = new OpenCvInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        //gets the camera ready and views the skystones
        phoneCam.openCameraDevice();
        skyStoneDetector = new CustomSkystoneDetector();
        skyStoneDetector.useDefaults();
        phoneCam.setPipeline(skyStoneDetector);
        phoneCam.startStreaming(320, 240, OpenCvCameraRotation.SIDEWAYS_RIGHT);


        // initialize these variables when the camera figures out
        // what the skystonePosition is (see while loop below)
        Pose2d skystonePositionWall; // position of skystone closest to the wall
        Pose2d skystonePositionBridge; // position of skystone closest to the bridge

        /*
        skystonePos for BLUE DEPOT SIDE, RED SKYBRIDGE
        - each stone is 8 in long
        1 (-29, -39)
        2 (-37, -39)
        3 (-45, -39)
        4 (-53, -39)
        5 (-61, -39)
        6 (-69, -39)
         */


        double positionCorrector = 0;
        //All comments comment above what is being commented

        while (!isStopRequested() && !opModeIsActive()) {
            xPosition = skyStoneDetector.foundRectangle().x;
            yPosition = skyStoneDetector.foundRectangle().y;

            if (xPosition >= 180 || xPosition < 40) {
                skystonePosition = "right";
                //positionCorrector = 14;
            } else if (xPosition > 130) {//x = 12
                skystonePosition = "center";
                //positionCorrector = 8;
            } else {
                skystonePosition = "left";
                //positionCorrector = 0;
            }

            telemetry.addData("xPos", xPosition);
            telemetry.addData("yPos", yPosition);
            telemetry.addData("SkyStone Pos", skystonePosition);
            telemetry.update();
        }
        //TODO Find what numbers we need to set the positionCorrector to for each skystone position, then incorporate that into our spline code


        waitForStart();

        if (isStopRequested()) return;

        /*
        3 stone auto code blue side:

        1. Move to skystonePositionWall
        2. Pick up skystone there
            robot.sideBackGripper.setPosition(OmegaBotRR.SIDE_BACK_GRIPPER_OPEN);
            robot.sideBackElbow.setPosition(OmegaBotRR.SIDE_BACK_ELBOW_DOWN);
            robot.sideBackGripper.setPosition(OmegaBotRR.SIDE_BACK_GRIPPER_CLOSED);
            robot.sideBackElbow.setPosition(OmegaBotRR.SIDE_BACK_ELBOW_UP);
        3. Move next to foundation (set a constant to that Pose2d position)
        4. Dump skystone on foundation
            robot.sideBackElbow.setPosition(OmegaBotRR.SIDE_BACK_ELBOW_DOWN);
            robot.sideBackGripper.setPosition(OmegaBotRR.SIDE_BACK_GRIPPER_OPEN);
            robot.sideBackElbow.setPosition(OmegaBotRR.SIDE_BACK_ELBOW_UP);
        5. Move to skystonePositionBridge
            - put elbow in ready position
        6. Pick up skystone there
            robot.sideFrontElbow.setPosition(OmegaBotRR.SIDE_FRONT_ELBOW_DOWN);
            robot.sideFrontGripper.setPosition(OmegaBotRR.SIDE_FRONT_GRIPPER_CLOSED);
            robot.sideBackElbow.setPosition(OmegaBotRR.SIDE_BACK_ELBOW_UP);
        7. Move back to foundation (set a constant to that Pose2d position)
        8. Dump skystone on foundation
            robot.sideBackElbow.setPosition(OmegaBotRR.SIDE_BACK_ELBOW_DOWN);
            robot.sideBackGripper.setPosition(OmegaBotRR.SIDE_BACK_GRIPPER_OPEN);
            robot.sideBackElbow.setPosition(OmegaBotRR.SIDE_BACK_ELBOW_UP);
        9. Move to closest regular stone (set a constant to that Pose2d position)
            - put elbow in ready position
        10. Pick up stone there
            robot.sideBackElbow.setPosition(OmegaBotRR.SIDE_BACK_ELBOW_DOWN);
            robot.sideBackGripper.setPosition(OmegaBotRR.SIDE_BACK_GRIPPER_CLOSED);
            robot.sideBackElbow.setPosition(OmegaBotRR.SIDE_BACK_ELBOW_UP);
        11. Dump skystone on foundation
            robot.sideBackElbow.setPosition(OmegaBotRR.SIDE_BACK_ELBOW_DOWN);
            robot.sideBackGripper.setPosition(OmegaBotRR.SIDE_BACK_GRIPPER_OPEN);
            robot.sideBackElbow.setPosition(OmegaBotRR.SIDE_BACK_ELBOW_UP);
        12. Pull foundation into building zone
            // move closer to foundation?
            robot.foundationGrippers.setPosition(OmegaBotRR.FOUNDATION_GRIPPERS_DOWN);
            // pull foundation into building zone by turning smartly
            robot.foundationGrippers.setPosition(OmegaBotRR.FOUNDATION_GRIPPERS_UP);
        13. Park under skybridge (set a constant to that Pose2d position)
         */

        // set initial position
        drive.setPoseEstimate(ROBOT_INIT_POSITION);

        // commands inside the trajectory run one after another
        drive.followTrajectorySync(
                drive.trajectoryBuilder()
                        .strafeTo(new Vector2d(-39,-39)) // strafe to coordinates (without turning)
                        .splineTo(new Pose2d(55,-35, 0)) // spline to coordinates (turns
                        .reverse() // makes robot go in reverse direction (rather than turning around to go to new Pose2d)
                        .splineTo(new Pose2d(-52,-39,0))//goes to pick up second block
                        .reverse()//reverses the reverse so it's normal again
                         .splineTo(new Pose2d(49,-35,0))//goes to drop of second block
                        .build()

        );

        drive.followTrajectorySync(
                drive.trajectoryBuilder()
                        .reverse()//reverses the robot to go pick up the third block
                        .splineTo(new Pose2d(-20,-35,0))//goes to pick up third block
                        .reverse()//reverses the previous reverse to get it back to normal
                        .splineTo(new Pose2d(40,-35,0))//goes to drop off 3d block
                        .build()
        );
    }
}