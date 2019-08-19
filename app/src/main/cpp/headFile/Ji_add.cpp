//
// Created by 今夜犬吠 on 2019/7/31.
//

#include "Ji_add.h"

Ji_add::Ji_add() {
    this->mSelf = 3;
}

Ji_add::Ji_add(int c) {
    this->mSelf = c;
}

int Ji_add::ToolAdd(int a, int b) {
    for (int i = 0; i < a; i++) {
        b++;
    }
    return b * 2 - this->mSelf;
}

