package com.example.lenovo.enclosure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 路径绘制
 * Created by kaidaye on 2017/4/19.
 */
public class PathView extends View {
    /**
     * 线条颜色值
     */
    private int paintColor = 0xff4598e5;
    /**
     * 第二条线颜色值
     */
    private int paintTransparentColor = 0x154598e5;

    /**
     * 线条宽度
     */
    private float strokeWidth = 15.0f;
    /**
     * 第二条线宽度
     */
    private float xustrokeWidth = 200.0f;
    /**
     * 透明度
     */
    private int alpha = 80;

    /**
     * 闭合X坐标距
     */
    private int closeX = 300;
    /**
     * 闭合Y坐标距
     */
    private int closeY = 300;
    /**
     * 是否是闭合只需要闭合状态
     */
    private Boolean isClose = false;

    /**
     * 当前闭合状态，true表示闭合，false 表示未闭合
     */
    private Boolean isStatus = false;


    private Paint mPaint = new Paint();
    private Paint transparentPaint = new Paint();
    private Path pathCircle = new Path();
    private Path mPath = new Path();
    private Path transparentPath = new Path();
    private Region region = new Region();

    private float pathX;
    private float pathY;

    private float startX;
    private float startY;

    private Boolean isDrwable = false;
    private Boolean isActionUp = false;
    private OnFinishListener listener;

    public PathView(Context context) {
        super(context);
        init();
    }

    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        //设置画笔颜色
        mPaint.setColor(paintColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeWidth);

        transparentPaint.setColor(paintTransparentColor);
        transparentPaint.setStyle(Paint.Style.STROKE);
        transparentPaint.setStrokeCap(Paint.Cap.ROUND);
        transparentPaint.setStrokeWidth(xustrokeWidth);
        transparentPaint.setAlpha(alpha);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
        if (isDrwable) {
            canvas.drawPath(transparentPath, transparentPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isActionUp) {
            isActionUp = false;
            mPaint.setStyle(Paint.Style.STROKE);
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                if (isClose) {//只需要闭合状态
                    mPath.close();
                    transparentPath.close();
                    transparentPaint.setStyle(Paint.Style.FILL);
                    transparentPaint.setStrokeWidth(strokeWidth);
                    isStatus = true;
                    RectF r = new RectF();
                    mPath.computeBounds(r, true);
                    //设置区域路径和剪辑描述的区域
                    region.setPath(mPath, new Region((int) r.left, (int) r.top, (int) r.right, (int) r.bottom));
                } else {//闭合与非闭合两种状态
                    if (Math.abs(startX - event.getX()) <= closeX && Math.abs(startY - event.getY()) <= closeY) {//距离较近可以闭合
                        mPath.close();
                        transparentPath.close();
                        transparentPaint.setStyle(Paint.Style.FILL);
                        transparentPaint.setStrokeWidth(strokeWidth);
                        isStatus = true;
                        RectF r = new RectF();
                        mPath.computeBounds(r, true);
                        //设置区域路径和剪辑描述的区域
                        region.setPath(mPath, new Region((int) r.left, (int) r.top, (int) r.right, (int) r.bottom));
                    } else {
                        RectF rCircle = new RectF();
                        pathCircle.computeBounds(rCircle, true);
                        region.setPath(pathCircle, new Region((int) rCircle.left, (int) rCircle.top, (int) rCircle.right, (int) rCircle.bottom));
                        isStatus = false;
                    }
                }
                isDrwable = true;

                if (listener != null)
                    listener.onFinish(region);
                break;
        }
        postInvalidate();
        return true;
    }

    //手指在屏幕上滑动时调用
    private void touchMove(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();
        pathCircle.addCircle(event.getX(), event.getY(), (xustrokeWidth - strokeWidth) / 2, Path.Direction.CCW);
        final float dx = Math.abs(x - pathX);
        final float dy = Math.abs(y - pathY);
        //两点之间的距离大于等于3时，生成贝塞尔绘制曲线
        if (dx >= 3 || dy >= 3) {
            //设置贝塞尔曲线的操作点为起点和终点的一半
            float cX = (x + pathX) / 2;
            float cY = (y + pathY) / 2;
            //二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
            mPath.quadTo(pathX, pathY, cX, cY);
            transparentPath.quadTo(pathX, pathY, cX, cY);
            //第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
            pathX = x;
            pathY = y;
        }
    }

    private void touchDown(MotionEvent event) {
        reset();
        pathX = event.getX();
        pathY = event.getY();
        startX = event.getX();
        startY = event.getY();
        mPath.moveTo(pathX, pathY);
        transparentPath.moveTo(pathX, pathY);
        isActionUp = true;
    }

    /**
     * 清除所有内容
     */
    public void reset() {
        mPath.reset();
        transparentPath.reset();
        pathCircle.reset();
        init();
        isDrwable = false;
    }


    public Boolean getClose() {
        return isClose;
    }

    /**
     * 是否只是闭合状态
     *
     * @param close
     */
    public void setClose(Boolean close) {
        isClose = close;
    }

    public int getPaintColor() {
        return paintColor;
    }

    /**
     * 设置画笔颜色
     *
     * @param paintColor
     */
    public void setPaintColor(int paintColor) {
        this.paintColor = paintColor;
    }

    public int getPaintTransparentColor() {
        return paintTransparentColor;
    }

    /**
     * 设置粗画笔颜色
     *
     * @param paintTransparentColor
     */
    public void setPaintTransparentColor(int paintTransparentColor) {
        this.paintTransparentColor = paintTransparentColor;
    }

    public int getCloseX() {
        return closeX;
    }

    /**
     * 设置闭合X距离
     *
     * @param closeX
     */
    public void setCloseX(int closeX) {
        this.closeX = closeX;
    }

    public int getCloseY() {
        return closeY;
    }

    /**
     * 设置闭合Y距离
     *
     * @param closeY
     */
    public void setCloseY(int closeY) {
        this.closeY = closeY;
    }

    /**
     * 当前闭合状态
     *
     * @return
     */
    public Boolean getStatus() {
        return isStatus;
    }

    public void setStatus(Boolean status) {
        isStatus = status;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * 设置线条宽度
     *
     * @param strokeWidth
     */
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        mPaint.setStrokeWidth(strokeWidth);
    }

    public float getXustrokeWidth() {
        return xustrokeWidth;
    }

    /**
     * 设置第二条线宽度
     *
     * @param xustrokeWidth
     */
    public void setXustrokeWidth(float xustrokeWidth) {
        this.xustrokeWidth = xustrokeWidth;
        transparentPaint.setStrokeWidth(xustrokeWidth);
    }

    /**
     * 设置第二条线透明度
     *
     * @param alpha
     */
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        transparentPaint.setAlpha(alpha);
    }

    public void setOnFinishListener(OnFinishListener listener) {
        this.listener = listener;
    }

    public interface OnFinishListener {
        public void onFinish(Region p);
    }

}
