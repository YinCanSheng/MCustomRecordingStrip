# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

#指定cmake版本
cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#添加头文件
#include_directories(src/main/cpp/include/ffmpeg)
#指定生成abi的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

#添加要编译的库别名 库类型 动态或静态 源码路径
#添加自己写的本地库
add_library( # 设置so库的名字
        jinye-lib

        # 设置库为动态链接库
        SHARED

        # 源文件的相对路径 src/mian/cpp/c文件 so文件
        native-lib.cpp)

#添加三库的本地库
#add_library(
#       avcodec
#        STATIC
#        IMPORTED
#)
#set_target_properties(
#        avcodec
#        PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavcodec-56.so
#)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

#查看系统库
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

#连接所有库编译 三库填写库名 系统库要使用${库名}
target_link_libraries( # Specifies the target library.
        jinye-lib
       # avcodec
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})