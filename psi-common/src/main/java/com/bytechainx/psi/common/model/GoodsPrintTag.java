package com.bytechainx.psi.common.model;

import java.util.List;

import com.bytechainx.psi.common.EnumConstant.PrintTemplateOrderTypeEnum;
import com.bytechainx.psi.common.model.base.BaseGoodsPrintTag;
import com.jfinal.kit.Kv;

/**
 * 商品打印标签
 */
@SuppressWarnings("serial")
public class GoodsPrintTag extends BaseGoodsPrintTag<GoodsPrintTag> {
	
	public static final GoodsPrintTag dao = new GoodsPrintTag().dao();
	
	public GoodsPrintTag findById(Integer id) {
		return GoodsPrintTag.dao.findFirst("select * from goods_print_tag where id = ?", id);
	}
	
	public GoodsInfo getGoodsInfo() {
		return GoodsInfo.dao.findById(getGoodsInfoId());
	}
	
	public GoodsUnit getGoodsUnit() {
		return GoodsUnit.dao.findById(getUnitId());
	}
	
	public String getGoodsSpecIds() {
		StringBuffer sb = new StringBuffer();
		if(getSpec1Id() != null && getSpec1Id() > 0) {
			sb.append(getSpec1Id()+":"+getSpecOption1Id());
		}
		if(getSpec2Id() != null && getSpec2Id() > 0) {
			sb.append("|");
			sb.append(getSpec2Id()+":"+getSpecOption2Id());
		}
		if(getSpec3Id() != null && getSpec3Id() > 0) {
			sb.append("|");
			sb.append(getSpec3Id()+":"+getSpecOption3Id());
		}
		return sb.toString();
	}
	
	public String getGoodsSpecNames() {
		GoodsInfo goodsInfo = getGoodsInfo();
		if(!goodsInfo.getSpecFlag()) { // 单规格
			return goodsInfo.getSpecName();
		}
		StringBuffer sb = new StringBuffer();
		if(getSpec1Id() != null && getSpec1Id() > 0) {
			sb.append(getSpecOption1().getOptionValue());
		}
		if(getSpec2Id() != null && getSpec2Id() > 0) {
			sb.append("/");
			sb.append(getSpecOption2().getOptionValue());
		}
		if(getSpec3Id() != null && getSpec3Id() > 0) {
			sb.append("/");
			sb.append(getSpecOption3().getOptionValue());
		}
		return sb.toString();
	}
	
	public GoodsSpecOptions getSpecOption1() {
		return GoodsSpecOptions.dao.findById(getSpecOption1Id());
	}
	public GoodsSpecOptions getSpecOption2() {
		return GoodsSpecOptions.dao.findById(getSpecOption2Id());
	}
	public GoodsSpecOptions getSpecOption3() {
		return GoodsSpecOptions.dao.findById(getSpecOption3Id());
	}

	/**
	 * 打印数据
	 * @return
	 */
	public Kv getPrintData() {
		Kv kv = Kv.create();
		GoodsInfo goodsInfo = getGoodsInfo();
		kv.set("goods_bar_code", getBarCode());
        kv.set("goods_name", goodsInfo.getName());
        kv.set("goods_spec", getGoodsSpecNames());
        kv.set("goods_attribute", "");
        GoodsUnit unit = getGoodsUnit();
        if(unit != null) {
        	kv.set("unit_name", unit.getName());
        }
        kv.set("price", getSalePrice());
        kv.set("goods_number", getGoodsNumber());
        return kv;
	}
	
	/**
	 * 获取默认打印模板
	 * @return
	 */
	public TenantPrintTemplate getPrintDefaultTemplate() {
		TenantPrintTemplate printTemplate = TenantPrintTemplate.dao.findDefault( PrintTemplateOrderTypeEnum.goods_tag.getValue());
        if (printTemplate == null) {
        	printTemplate = TenantPrintTemplate.dao.findDefault( PrintTemplateOrderTypeEnum.goods_tag.getValue());
        }
        return printTemplate;
	}

	/**
	 * 打印模板列表
	 * @return
	 */
	public List<TenantPrintTemplate> getPrintTemplateList() {
		return TenantPrintTemplate.dao.find("select * from tenant_print_template where order_type = ?", PrintTemplateOrderTypeEnum.goods_tag.getValue());
	}
	
}

