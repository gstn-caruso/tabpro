package com.gstncaruso.tabpro.ui.score;

public interface ZoomHolder {

    Zoom zoom();

    void setZoom(Zoom zoom);

    void onZoomChange(Runnable listener);
}
