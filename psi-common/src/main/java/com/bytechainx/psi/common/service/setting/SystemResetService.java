package com.bytechainx.psi.common.service.setting;


import java.io.File;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.CommonConfig;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.EnumConstant.GoodsAttrTypeEnum;
import com.bytechainx.psi.common.EnumConstant.PrintModeTypeEnum;
import com.bytechainx.psi.common.EnumConstant.PrintTemplateOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.SalePayTypeEnum;
import com.bytechainx.psi.common.kit.PinYinUtil;
import com.bytechainx.psi.common.model.CustomerCategory;
import com.bytechainx.psi.common.model.CustomerInfo;
import com.bytechainx.psi.common.model.CustomerPriceLevel;
import com.bytechainx.psi.common.model.GoodsAttribute;
import com.bytechainx.psi.common.model.GoodsCategory;
import com.bytechainx.psi.common.model.GoodsSpec;
import com.bytechainx.psi.common.model.GoodsSpecOptions;
import com.bytechainx.psi.common.model.GoodsUnit;
import com.bytechainx.psi.common.model.SupplierCategory;
import com.bytechainx.psi.common.model.SupplierInfo;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderFundType;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Ret;

import cn.hutool.core.io.FileUtil;


/**
* 系统重置
*/
public class SystemResetService extends CommonService {

	/**
	 * 初始化租户的基本数据
	 * @param tenantOrgId
	 * @return
	 */
	public Ret initTenantData() {
		String[] accountNams = new String[] { "现金", "微信", "支付宝", "在线支付" };

		for (String name : accountNams) {
			// 创建结算帐户
			TraderBalanceAccount balanceAccount = new TraderBalanceAccount();
			balanceAccount.setName(name);
			balanceAccount.setCode(PinYinUtil.getFirstSpell(name));
			balanceAccount.setAccountName("");
			balanceAccount.setAccountNo("");
			balanceAccount.setCreatedAt(new Date());
			balanceAccount.setDefaultFlag(FlagEnum.YES.getValue());
			balanceAccount.setPosition(100);
			balanceAccount.setUpdatedAt(new Date());
			if(StringUtils.equals(name, "在线支付")) { // 默认禁用，等开通后再开启
				balanceAccount.setDataStatus(DataStatusEnum.disable.getValue());
			}
			balanceAccount.save();

		}

		Integer customerPriceLevelId1 = 0; // 零售价ID
		Integer customerPriceLevelId2 = 0; // 批发价ID

		// 创建客户价格等级
		String[] priceLevels = new String[] { "零售价", "批发价" };
		for (String level : priceLevels) {
			CustomerPriceLevel customerPriceLevel = new CustomerPriceLevel();
			customerPriceLevel.setCreatedAt(new Date());
			customerPriceLevel.setDefaultFlag(FlagEnum.YES.getValue());
			customerPriceLevel.setDataStatus(DataStatusEnum.enable.getValue());
			customerPriceLevel.setName(level);
			customerPriceLevel.setUpdatedAt(new Date());
			customerPriceLevel.save();

			if (level.equals("零售价")) {
				customerPriceLevelId1 = customerPriceLevel.getId();
			}
			if (level.equals("批发价")) {
				customerPriceLevelId2 = customerPriceLevel.getId();
			}
		}
		Integer customerCategoryId = null;
		String[] customerCategoryNames = new String[] { "默认分类" };
		for (String customerCategoryName : customerCategoryNames) {
			CustomerCategory customerCategory = new CustomerCategory();
			customerCategory.setCreatedAt(new Date());
			customerCategory.setName(customerCategoryName);
			customerCategory.setUpdatedAt(new Date());
			customerCategory.save();
			
			customerCategoryId = customerCategory.getId();
		}

		// 创建往来账帐户
		String[] custNames = new String[] { "零售客户", "批发客户" };
		for (String custName : custNames) {
			TraderBookAccount traderBookAccount = new TraderBookAccount();
			traderBookAccount.setCreatedAt(new Date());
			traderBookAccount.setInAmount(BigDecimal.ZERO);
			traderBookAccount.setOpenBalance(BigDecimal.ZERO);
			traderBookAccount.setOutAmount(BigDecimal.ZERO);
			traderBookAccount.setPayAmount(BigDecimal.ZERO);
			traderBookAccount.setUpdatedAt(new Date());
			traderBookAccount.save();

			CustomerInfo customer = new CustomerInfo();
			customer.setCreatedAt(new Date());
			customer.setDefaultFlag(FlagEnum.YES.getValue());
			customer.setName(custName);
			customer.setCode(PinYinUtil.getFirstSpell(custName));
			customer.setUpdatedAt(new Date());
			if (custName.equals("零售客户")) {
				customer.setCustomerPriceLevelId(customerPriceLevelId1);
			} else if (custName.equals("批发客户")) {
				customer.setCustomerPriceLevelId(customerPriceLevelId2);
			}
			customer.setDataStatus(DataStatusEnum.enable.getValue());
			customer.setDiscount(new BigDecimal("1"));
			customer.setMobile("");
			customer.setPayType(SalePayTypeEnum.nowCash.getValue());
			customer.setTraderBookAccountId(traderBookAccount.getId());
			customer.setCustomerCategoryId(customerCategoryId);
			customer.save();
		}
		// 创建默认零散供应商
		TraderBookAccount traderBookAccount = new TraderBookAccount();
		traderBookAccount.setCreatedAt(new Date());
		traderBookAccount.setInAmount(BigDecimal.ZERO);
		traderBookAccount.setOpenBalance(BigDecimal.ZERO);
		traderBookAccount.setOutAmount(BigDecimal.ZERO);
		traderBookAccount.setUpdatedAt(new Date());
		traderBookAccount.save();

		Integer supplierCategoryId = null;
		String[] supplierCategoryNames = new String[] { "默认分类" };
		for (String supplierCategoryName : supplierCategoryNames) {
			SupplierCategory supplierCategory = new SupplierCategory();
			supplierCategory.setCreatedAt(new Date());
			supplierCategory.setName(supplierCategoryName);
			supplierCategory.save();
			
			supplierCategoryId = supplierCategory.getId();
		}
		
		SupplierInfo supplier = new SupplierInfo();
		supplier.setCreatedAt(new Date());
		supplier.setDefaultFlag(FlagEnum.YES.getValue());
		supplier.setName("零散供应商");
		supplier.setCode(supplier.getName());
		supplier.setUpdatedAt(new Date());
		supplier.setMobile("");
		supplier.setDataStatus(DataStatusEnum.enable.getValue());
		supplier.setTraderBookAccountId(traderBookAccount.getId());
		supplier.setSupplierCategoryId(supplierCategoryId);
		supplier.save();
		
		// FIXME 商品的规格，单位，属性，可以根据不同行业创建默认的数据
		String[] unitNames = new String[] { "支", "只", "桶", "箱", "套", "斤", "件" };
		for (String unitName : unitNames) {
			GoodsUnit goodsUnit = new GoodsUnit();
			goodsUnit.setCreatedAt(new Date());
			goodsUnit.setDataStatus(DataStatusEnum.enable.getValue());
			goodsUnit.setName(unitName);
			goodsUnit.setUpdatedAt(new Date());
			goodsUnit.save();
		}
		String[] specNames = new String[] { "颜色" };
		for (String specName : specNames) {
			GoodsSpec goodsSpec = new GoodsSpec();
			goodsSpec.setCreatedAt(new Date());
			goodsSpec.setDataStatus(DataStatusEnum.enable.getValue());
			goodsSpec.setName(specName);
			goodsSpec.setUpdatedAt(new Date());
			goodsSpec.save();
			
			String[] specOptionsNames = new String[] { "红色", "白色", "绿色", "蓝色", "黑色", "黄色" };
			for (String specOptionsName : specOptionsNames) {
				GoodsSpecOptions goodsSpecOptions = new GoodsSpecOptions();
				goodsSpecOptions.setDataStatus(DataStatusEnum.enable.getValue());
				goodsSpecOptions.setGoodsSpecId(goodsSpec.getId());
				goodsSpecOptions.setOptionValue(specOptionsName);
				goodsSpecOptions.save();
			}
		}
		
		String[] goodsCategoryNames = new String[] { "默认分类"};
		for (String goodsCategoryName : goodsCategoryNames) {
			GoodsCategory goodsCategory = new GoodsCategory();
			goodsCategory.setCreatedAt(new Date());
			goodsCategory.setParentId(0);
			goodsCategory.setName(goodsCategoryName);
			goodsCategory.setUpdatedAt(new Date());
			goodsCategory.save();
		}
		
		String[] goodsAttributeNames = new String[] { "品牌" };
		for (String goodsAttributeName : goodsAttributeNames) {
			GoodsAttribute goodsAttribute = new GoodsAttribute();
			goodsAttribute.setCreatedAt(new Date());
			goodsAttribute.setAttrType(GoodsAttrTypeEnum.select.getValue());
			goodsAttribute.setAttrValues("其他");
			goodsAttribute.setDefaultFlag(FlagEnum.NO.getValue());
			goodsAttribute.setName(goodsAttributeName);
			goodsAttribute.setUpdatedAt(new Date());
			goodsAttribute.save();
		}
		
		String[] traderFundTypeNames = new String[] { "房租", "电费", "水费", "通讯费", "员工工资" };
		for (String traderFundTypeName : traderFundTypeNames) {
			TraderFundType traderFundType = new TraderFundType();
			traderFundType.setCreatedAt(new Date());
			traderFundType.setName(traderFundTypeName);
			traderFundType.setFundFlow(FundFlowEnum.expenses.getValue());
			traderFundType.setPostion(0);
			traderFundType.setUpdatedAt(new Date());
			traderFundType.save();
		}
		
		traderFundTypeNames = new String[] { "分红" };
		for (String traderFundTypeName : traderFundTypeNames) {
			TraderFundType traderFundType = new TraderFundType();
			traderFundType.setCreatedAt(new Date());
			traderFundType.setName(traderFundTypeName);
			traderFundType.setFundFlow(FundFlowEnum.income.getValue());
			traderFundType.setPostion(0);
			traderFundType.setUpdatedAt(new Date());
			traderFundType.save();
		}
		
		for(PrintTemplateOrderTypeEnum orderTypeEnum : PrintTemplateOrderTypeEnum.values()) {
			String content = FileUtil.readString(new File(CommonConfig.commonConfigPath+"/printTpl/"+orderTypeEnum.name()+".tpl"), "utf-8");
			String tplName = "二等分";
			String paperType = "241*140";
			if(orderTypeEnum.getValue() == PrintTemplateOrderTypeEnum.goods_tag.getValue()) {
				tplName = "小票标签";
				paperType = "80mm*116mm";
			}
			TenantPrintTemplate printTemplate = new TenantPrintTemplate();
			printTemplate.setContent(content);
			printTemplate.setCreatedAt(new Date());
			printTemplate.setDefaultFlag(FlagEnum.YES.getValue());
			printTemplate.setName(tplName);
			printTemplate.setOrderType(orderTypeEnum.getValue());
			printTemplate.setPaperType(paperType);
			printTemplate.setPrintMode(PrintModeTypeEnum.all.getValue());
			printTemplate.setUpdatedAt(new Date());
			printTemplate.save();
		}
		
		return Ret.ok();
	}


}