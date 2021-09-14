package com.begin.gulimall.common.constant;

/**
 * @author 83456
 */
public class ProductConstant {
   public enum AttrEnum{
       ATTR_TYPE_BASE(1,"基本属性"),
       ATTR_TYPE_SALE(0,"销售属性");

       private int code;
       private String message;
       AttrEnum(int code,String message){
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

    public enum StatusEnum{
        NEW_SPU(0,"新建"),
        SPU_UP(1,"商品上架"),
        SPU_DOWN(3,"商品下架");

        private int code;
        private String message;
        StatusEnum(int code,String message){
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
