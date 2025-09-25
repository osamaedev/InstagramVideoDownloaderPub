cmake_minimum_required(VERSION 3.22.1)
add_library(
        Main
        SHARED
        Main.cpp
)

find_library(
        Main.cpp
        log
)

target_link_libraries(
        Main
        ${log-lib}
)