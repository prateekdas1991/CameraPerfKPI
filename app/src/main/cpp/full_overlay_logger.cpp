#include <jni.h>
#include <fstream>
#include <mutex>
#include <android/log.h>

#define LOG_TAG "FullOverlayLogger"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static std::ofstream frameOut;
static std::mutex writeMutex;
static bool isInitialized = false;

const char* kOutPath = "/sdcard/overlay_frames_gray.dat";
const int kWidth = 350;
const int kHeight = 100;
const int kPixels = kWidth * kHeight;
const int kRGBA = kPixels * 4;
const int kGRAY = kPixels;

extern "C"
JNIEXPORT void JNICALL
Java_io_github_prateekdas1991_OverlayWriter_writeOverlayGrayFrame(
        JNIEnv *env, jobject /* thiz */, jbyteArray rgbaFrame) {

if (env->GetArrayLength(rgbaFrame) != kRGBA) {
LOGI("❌ Expected 140000 bytes, got %d", env->GetArrayLength(rgbaFrame));
return;
}

jbyte* rgba = env->GetByteArrayElements(rgbaFrame, nullptr);
if (!rgba) return;

uint8_t gray[kGRAY];

for (int i = 0; i < kPixels; ++i) {
uint8_t R = rgba[i * 4 + 0];
uint8_t G = rgba[i * 4 + 1];
uint8_t B = rgba[i * 4 + 2];
gray[i] = static_cast<uint8_t>(0.299f * R + 0.587f * G + 0.114f * B);
}

env->ReleaseByteArrayElements(rgbaFrame, rgba, JNI_ABORT);

std::lock_guard<std::mutex> lock(writeMutex);

if (!isInitialized) {
frameOut.open(kOutPath, std::ios::binary | std::ios::app);
if (!frameOut.is_open()) {
LOGI("❌ Failed to open %s", kOutPath);
return;
}
LOGI("✅ Dumping all grayscale overlay frames to %s", kOutPath);
isInitialized = true;
}

frameOut.write(reinterpret_cast<const char*>(gray), kGRAY);
frameOut.flush();
}