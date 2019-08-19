//
// Created by 今夜犬吠 on 2019/7/31.
//
//头文件里主要申明属性与方法 不做具体实现
#ifndef JIDUB_JI_ADD_H
#define JIDUB_JI_ADD_H


class Ji_add {

private:
    int mSelf;
    /*申明要实现的方法*/
public:
    /*构造函数*/
    Ji_add();
    Ji_add(int c);
    /*加法*/
    int ToolAdd(int a, int b);
};


#endif //JIDUB_JI_ADD_H
