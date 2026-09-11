package com.hakamo.drugs;

public class Drug {
    public String id, trade, tradeAr, generic, genericAr, active, form, company, category, indications, source, updatedAt, priceEgp, priceUpdatedAt, imageUrl;
    public Drug(String id,String trade,String tradeAr,String generic,String genericAr,String active,String form,String company,String category,String indications,String source,String updatedAt,String priceEgp,String priceUpdatedAt,String imageUrl){
        this.id=id; this.trade=trade; this.tradeAr=tradeAr; this.generic=generic; this.genericAr=genericAr; this.active=active; this.form=form; this.company=company; this.category=category; this.indications=indications; this.source=source; this.updatedAt=updatedAt; this.priceEgp=priceEgp; this.priceUpdatedAt=priceUpdatedAt; this.imageUrl=imageUrl;
    }
    public String displayName(){ return tradeAr!=null&&!tradeAr.isEmpty()?tradeAr+(trade==null||trade.isEmpty()?"":" • "+trade):trade; }
}
