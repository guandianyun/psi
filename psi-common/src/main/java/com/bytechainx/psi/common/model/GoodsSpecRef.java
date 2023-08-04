package com.bytechainx.psi.common.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.model.base.BaseGoodsSpecRef;

/**
 * 商品规格关联
 */
@SuppressWarnings("serial")
public class GoodsSpecRef extends BaseGoodsSpecRef<GoodsSpecRef> {
	
	public static final GoodsSpecRef dao = new GoodsSpecRef().dao();
	
	public GoodsSpec getGoodsSpec() {
		return GoodsSpec.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "goods.spec.id."+getGoodsSpecId(), "select * from goods_spec where id = ?", getGoodsSpecId());
	}
	/**
	 * 是否包含选项值
	 * @param optionId
	 * @return
	 */
	public boolean isHasSpecOption(Integer optionId) {
		String ids = getSpecValue();
		ids = ids.startsWith(",") ? ids : ","+ids;
		ids = ids.endsWith(",") ? ids : ids+",";
		return ids.contains(","+optionId+",");
	}
	
	public List<GoodsSpecOptions> getSpecValueList() {
		String[] options = StringUtils.split(getSpecValue(), ",");
		if(options == null || options.length <= 0) {
			return null;
		}
		List<GoodsSpecOptions> goodsSpecOptions = new ArrayList<>();
		for (String option : options) {
			GoodsSpecOptions specOption = GoodsSpecOptions.dao.findById(Integer.parseInt(option));
			goodsSpecOptions.add(specOption);
		}
		return goodsSpecOptions;
	}
}

