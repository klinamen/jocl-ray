package io.klinamen.joclray.scene;

import io.klinamen.joclray.util.FloatVec4;

public class Camera {
    private float fovGrad = 90;
    private float frameWidth;
    private float frameHeight;
    private FloatVec4 from = new FloatVec4();
    private FloatVec4 to = new FloatVec4();

    public float getFovGrad() {
        return fovGrad;
    }

    public Camera setFovGrad(float fovGrad) {
        this.fovGrad = fovGrad;
        return this;
    }

    public float getFovRad(){
        return (float)(fovGrad * Math.PI / 180);
    }

    public float getFrameWidth() {
        return frameWidth;
    }

    public Camera setFrameWidth(float frameWidth) {
        this.frameWidth = frameWidth;
        return this;
    }

    public float getFrameHeight() {
        return frameHeight;
    }

    public Camera setFrameHeight(float frameHeight) {
        this.frameHeight = frameHeight;
        return this;
    }

    public FloatVec4 getFrom() {
        return from;
    }

    public Camera setFrom(FloatVec4 from) {
        this.from = from;
        return this;
    }

    public int getPixelIndex(float xPerc, float yPerc){
        return (int)(getFrameWidth() * xPerc) + (int)(getFrameHeight() * yPerc) * (int)(getFrameWidth());
    }

    public int getPixels() {
        return (int)getFrameWidth() * (int) getFrameHeight();
    }

    public FloatVec4 getTo() {
        return to;
    }

    public Camera setTo(FloatVec4 to) {
        this.to = to;
        return this;
    }
}
