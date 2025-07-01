cmake_minimum_required(VERSION 3.10.2)
project(full_overlay_logger)

add_library(overlay_writer SHARED full_overlay_logger.cpp)

# Required Android libs
find_library(log-lib log)

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--exclude-libs,libc++_shared.so")

target_link_libraries(overlay_writer ${log-lib} c++_shared)
