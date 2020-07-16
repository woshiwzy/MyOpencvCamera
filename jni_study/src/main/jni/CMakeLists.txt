
cmake_minimum_required(VERSION 3.4.1)
include_directories(${CMAKE_SOURCE_DIR}/include)

add_library(libopencv_java4 SHARED IMPORTED)

#注意${CMAKE_SOURCE_DIR} 是指的CmakeLists.txt的路径，必须要把路径搞对，不然依赖包找不到
# 会报no rule to make it *******
set_target_properties(libopencv_java4 PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../../../../cv42sdk/native/libs/${ANDROID_ABI}/libopencv_java4.so)

add_library(
             native-lib
             SHARED
             native-lib.cpp )


find_library( # Sets the name of the path variable.
              log-lib
              log )


target_link_libraries( # Specifies the target library.
                       native-lib
                       libopencv_java4
                       ${log-lib} )