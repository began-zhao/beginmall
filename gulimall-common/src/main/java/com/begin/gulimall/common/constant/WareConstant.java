package com.begin.gulimall.common.constant;

public class WareConstant {
    public enum PurchaseStatusEnum{
        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        RECEIVE(2,"已领取"),
        FINISH(3,"已完成"),
        HASERROR(4,"有异常"),
        ;

        private int code;
        private String message;
        PurchaseStatusEnum(int code,String message){
            this.code=code;
            this.message=message;
        }
        public int getCode(){
            return this.code;
        }
        public String getMssage(){
            return this.message;
        }
    }

    public enum PurchaseDetailStatusEnum{
        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        BUYING(2,"正在采购"),
        FINISH(3,"已完成"),
        HASERROR(4,"采购失败"),
        ;

        private int code;
        private String message;
        PurchaseDetailStatusEnum(int code,String message){
            this.code=code;
            this.message=message;
        }
        public int getCode(){
            return this.code;
        }
        public String getMssage(){
            return this.message;
        }
    }
}
