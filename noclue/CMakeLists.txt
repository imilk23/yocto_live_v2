cmake_minimum_required(VERSION 2.8)

project(noclue)

add_executable(noclue noclue.cpp)

install(TARGETS noclue DESTINATION bin)

