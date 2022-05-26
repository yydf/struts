package cn.coder.struts.support;

import cn.coder.struts.view.MultipartFile;

public interface FileUploadListener {

	String uploadMultipartFile(MultipartFile file);

}
