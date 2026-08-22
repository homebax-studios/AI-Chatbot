cmake_minimum_required(VERSION 3.22.1)

project("axionis")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Axionis JNI library
add_library(
        axionis
        SHARED
        llama-jni.cpp
        whisper-jni.cpp
)

# Android log library
find_library(
        log-lib
        log
)

target_link_libraries(
        axionis
        ${log-lib}
)

# llama.cpp
add_subdirectory(
        ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp
        ${CMAKE_CURRENT_BINARY_DIR}/llama.cpp
)

# whisper.cpp
add_subdirectory(
        ${CMAKE_CURRENT_SOURCE_DIR}/whisper.cpp
        ${CMAKE_CURRENT_BINARY_DIR}/whisper.cpp
)