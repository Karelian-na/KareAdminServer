package cn.karelian.kas.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.karelian.kas.KasApplication;
import cn.karelian.kas.Result;
import cn.karelian.kas.codes.FieldErrors;
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.entities.TableFieldsInfo;
import cn.karelian.kas.entities.ViewsInfo;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.IllegalAccessException;
import cn.karelian.kas.exceptions.PermissionNotFoundException;
import cn.karelian.kas.exceptions.TransactionFailedException;
import cn.karelian.kas.mappers.ViewsInfoMapper;
import cn.karelian.kas.services.interfaces.IDatabasesService;
import cn.karelian.kas.utils.EntityUtil;
import cn.karelian.kas.utils.ExceptionUtil;
import cn.karelian.kas.utils.LoginInfomationUtil;
import cn.karelian.kas.utils.MybatisPlusUtil;
import cn.karelian.kas.utils.PageData;
import cn.karelian.kas.utils.WebPageInfo;
import cn.karelian.kas.views.FieldsInfoView;
import cn.karelian.kas.views.Views;

@Service
public class DatabasesService extends KasService<ViewsInfoMapper, ViewsInfo, Views> implements IDatabasesService {
	private Logger logger = LoggerFactory.getLogger(DatabasesService.class);

	@Autowired
	private TableFieldsInfoService tableFieldsInfoService;

	@Override
	public Result index(IndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		Result result = new Result();
		PageData<Views> pageData = new PageData<>();
		if (params.initPageSize == null) {
			pageData.data = baseMapper.selectViewList(null);
			result.setData(pageData);
		} else {
			WebPageInfo<Views> info = super.getWebPageInfo(Views.class);
			info.pageData = pageData;
			info.pageData.data = baseMapper.selectViewList(null);
			result.setData(info);
		}
		result.setSuccess(true);
		pageData.totalCount = Integer.valueOf(pageData.data.size()).longValue();
		return result;
	}

	@Override
	public Result editindex(String viewName) {
		FieldErrors err = EntityUtil.CheckStringField(viewName, 1, 30, true);
		if (err != null) {
			return Result.fieldError("viewName", err);
		}

		Result result = new Result();

		LambdaQueryWrapper<Views> vlqw = new LambdaQueryWrapper<>();
		vlqw.eq(Views::getView_name, viewName);
		Views view = baseMapper.selectViewOne(vlqw);
		if (view == null) {
			result.setMsg("请求视图不存在!");
			return result;
		}

		Map<String, Object> data = new HashMap<>();

		LambdaQueryWrapper<FieldsInfoView> lqw = new LambdaQueryWrapper<>();
		lqw.eq(FieldsInfoView::getTable_name, viewName);
		data.put("fields", tableFieldsInfoService.getBaseMapper().selectViewList(lqw));

		result.setSuccess(true);
		if (!ObjectUtils.isEmpty(view.getFields_config())) {
			Path fieldConfigPath = KasApplication.currentPath.resolve("data/configs").resolve(view.getFields_config());
			if (Files.exists(fieldConfigPath)) {
				try (FileInputStream fileInputStream = new FileInputStream(fieldConfigPath.toFile())) {
					String content = new String(fileInputStream.readAllBytes(), "utf-8");
					data.put("fields_config", content);
				} catch (Exception e) {
					result.setSuccess(false);
					result.setMsg("无法读取字段配置文件！");
					Logger logger = LoggerFactory.getLogger(DatabasesService.class);
					logger.error("Failed to read file `" + fieldConfigPath.toString() + "`, reason: " + e.getMessage());
				}
			}
		}

		result.setData(data);
		return result;
	}

	@Override
	@Transactional(rollbackFor = TransactionFailedException.class)
	public Result edit(Views params) throws TransactionFailedException {
		Result result = new Result();
		Views oldView = baseMapper.selectViewOne(Wrappers.lambdaQuery((Views) null)
				.eq(Views::getView_name, params.getView_name()));

		if (oldView == null) {
			result.setMsg("请求视图不存在!");
			return result;
		}

		if (EntityUtil.IsNonOrEmpty(params, "viewName")) {
			result.fail("修改内容为空！");
			return result;
		}

		// 更新视图
		ViewsInfo viewInfo = new ViewsInfo();
		BeanUtils.copyProperties(params, viewInfo, "viewName", "fields_config");

		var luw = Wrappers.update((ViewsInfo) null).eq("view_name", params.getView_name());
		MybatisPlusUtil.applyNonNullUpdateFields(viewInfo, luw);
		result.setSuccess(this.saveOrUpdate(viewInfo, luw));
		if (!result.isSuccess()) {
			result.setMsg("更新试图失败！");
			throw new TransactionFailedException(result);
		}

		// 更新字段列表
		if (params.fields != null) {
			for (String key : params.fields.keySet()) {
				TableFieldsInfo info = params.fields.get(key);
				info.setField_name(key);
				info.setTable_name(params.getView_name());
				FieldErrors err = EntityUtil.CheckStringField(info.getDisplay_name(), 2, 15, false);
				if (err != null) {
					return Result.fieldError("fields." + key + ".field_name", err);
				} else if (info.getEditable() != null && info.getEditable()
						&& key.matches("((.*_id)|.*(user|time))$")) {
					result.setMsg("不能修改字段" + key + "的可编辑性!");
					return result;
				}
			}

			List<String> existsFields = tableFieldsInfoService.listObjs(
					tableFieldsInfoService.lambdaQuery()
							.select(TableFieldsInfo::getField_name)
							.eq(TableFieldsInfo::getTable_name, params.getView_name())
							.getWrapper(),
					v -> (String) v);

			var partitioned = params.fields.values().stream()
					.collect(Collectors.partitioningBy(v -> existsFields.contains(v.getField_name())));

			LambdaUpdateWrapper<TableFieldsInfo> fieldLuw = new LambdaUpdateWrapper<>();
			var updateItems = partitioned.get(true);
			if (updateItems.size() != 0) {
				for (int idx = 0; idx < updateItems.size(); idx++) {
					TableFieldsInfo item = updateItems.get(idx);
					fieldLuw.eq(TableFieldsInfo::getTable_name, params.getView_name())
							.eq(TableFieldsInfo::getField_name, item.getField_name());

					result.setSuccess(tableFieldsInfoService.update(item, fieldLuw));
					if (!result.isSuccess()) {
						result.setMsg("更新字段失败！");
						throw new TransactionFailedException(result);
					}

					fieldLuw.clear();
				}
			}

			var addItems = partitioned.get(false);
			if (addItems.size() != 0) {
				result.setSuccess(tableFieldsInfoService.saveBatch(addItems));
				if (!result.isSuccess()) {
					result.setMsg("更新字段失败！");
					throw new TransactionFailedException(result);
				}
			}
		}

		// 更新字段配置，放在最后，为文件更改
		do {
			if (ObjectUtils.isEmpty(params.getFields_config())) {
				break;
			}

			String strRelativePath = null;
			if (ObjectUtils.isEmpty(oldView.getFields_config())) {
				strRelativePath = "fields/" + params.getView_name() + ".js";
				viewInfo.setFields_config(strRelativePath);
			} else {
				strRelativePath = oldView.getFields_config();
			}
			Path fieldConfigPath = KasApplication.currentPath.resolve("data/configs").resolve(strRelativePath);

			if (Files.exists(fieldConfigPath)) {
				String suffix = "." + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".bak";
				Path backupFieldsConfigPath = Path.of(fieldConfigPath.toString() + suffix);
				try {
					Files.copy(fieldConfigPath, backupFieldsConfigPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					logger.error("Failed to backup the fields config, details" + ExceptionUtil.getMessage(e));
					result.fail("备份配置失败，无法完成修改！");
					throw new TransactionFailedException(result);
				}
			}

			try (FileOutputStream fileOutputStream = new FileOutputStream(fieldConfigPath.toFile())) {
				fileOutputStream.write(params.getFields_config().getBytes("utf-8"));
				result.setSuccess(true);
			} catch (Exception e) {
				logger.error("Failed to write fields config, details: " + ExceptionUtil.getMessage(e));
				result.fail("更新字段配置失败！");
				throw new TransactionFailedException(result);
			}
		} while (false);

		if (params.fields != null) {
			tableFieldsInfoService.reloadFieldsConfigs();
		}

		BeanUtils.copyProperties(viewInfo, params);
		params.setUpdate_user(LoginInfomationUtil.getUserName());
		result.setData(params);
		return result;
	}

	@Override
	public Result reloadFieldsInfo() {
		tableFieldsInfoService.reloadFieldsConfigs();

		return new Result(true);
	}
}
