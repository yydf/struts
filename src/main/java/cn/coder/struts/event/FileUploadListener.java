package cn.coder.struts.event;

import cn.coder.struts.mvc.MultipartFile;

public interface FileUploadListener {

	String uploadMultipartFile(MultipartFile file);

}
