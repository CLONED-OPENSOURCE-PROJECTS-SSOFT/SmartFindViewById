package utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.*;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.JBColor;
import entity.ViewWidgetElement;
import org.apache.http.util.TextUtils;

import java.awt.*;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    // 通过strings.xml获取的值
    private static String StringValue;

    /**
     * 显示dialog
     *
     * @param editor
     * @param result 内容
     * @param time   显示时间，单位秒
     */
    public static void showPopupBalloon(final Editor editor, final String result, final int time) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder(result, null, new JBColor(new Color(116, 214, 238), new Color(76, 112, 117)), null)
                        .setFadeoutTime(time * 1000)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
            }
        });
    }

    /**
     * 驼峰
     *
     * @param fieldName
     * @return
     */
    public static String getFieldName(String fieldName) {
        if (!TextUtils.isEmpty(fieldName)) {
            String[] names = fieldName.split("_");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < names.length; i++) {
                sb.append(firstToUpperCase(names[i]));
            }
            fieldName = sb.toString();
        }
        return fieldName;
    }

    /**
     * 第一个字母大写
     *
     * @param key
     * @return
     */
    public static String firstToUpperCase(String key) {
        return key.substring(0, 1).toUpperCase(Locale.CHINA) + key.substring(1);
    }

    /**
     * 解析xml获取string的值
     *
     * @param psiFile
     * @param text
     * @return
     */
    public static String getTextFromStringsXml(PsiFile psiFile, String text) {
        psiFile.accept(new XmlRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (element instanceof XmlTag) {
                    XmlTag tag = (XmlTag) element;
                    if (tag.getName().equals("string")
                            && tag.getAttributeValue("name").equals(text)) {
                        PsiElement[] children = tag.getChildren();
                        String value = "";
                        for (PsiElement child : children) {
                            value += child.getText();
                        }
                        // value = <string name="app_name">My Application</string>
                        // 用正则获取值
                        Pattern p = Pattern.compile("<string name=\"" + text + "\">(.*)</string>");
                        Matcher m = p.matcher(value);
                        while (m.find()) {
                            StringValue = m.group(1);
                        }
                    }
                }
            }
        });
        return StringValue;
    }


    /**
     * layout.getValue()返回的值为@layout/layout_view
     *
     * @param layout
     * @return
     */
    public static String getLayoutName(String layout) {
        if (layout == null || !layout.startsWith("@") || !layout.contains("/")) {
            return null;
        }

        // @layout layout_view
        String[] parts = layout.split("/");
        if (parts.length != 2) {
            return null;
        }
        // layout_view
        return parts[1];
    }

    /**
     * 根据当前文件获取对应的class文件
     *
     * @param editor
     * @param file
     * @return
     */
    public static PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        } else {
            PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ? null : target;
        }
    }

    /**
     * 判断mClass是不是继承activityClass或者activityCompatClass
     *
     * @param mProject
     * @param mClass
     * @return
     */
    public static boolean isExtendsActivityOrActivityCompat(Project mProject, PsiClass mClass) {
        // 根据类名查找类
        PsiClass activityClass = JavaPsiFacade.getInstance(mProject).findClass("android.app.Activity", new EverythingGlobalScope(mProject));
        PsiClass activityCompatClass = JavaPsiFacade.getInstance(mProject).findClass("android.support.v7.app.AppCompatActivity", new EverythingGlobalScope(mProject));
        return (activityClass != null && mClass.isInheritor(activityClass, true))
                || (activityCompatClass != null && mClass.isInheritor(activityCompatClass, true))
                || mClass.getName().contains("Activity");
    }

    /**
     * 判断mClass是不是继承fragmentClass或者fragmentV4Class
     *
     * @param mProject
     * @param mClass
     * @return
     */
    public static boolean isExtendsFragmentOrFragmentV4(Project mProject, PsiClass mClass) {
        // 根据类名查找类
        PsiClass fragmentClass = JavaPsiFacade.getInstance(mProject).findClass("android.app.Fragment", new EverythingGlobalScope(mProject));
        PsiClass fragmentV4Class = JavaPsiFacade.getInstance(mProject).findClass("android.support.v4.app.Fragment", new EverythingGlobalScope(mProject));
        return (fragmentClass != null && mClass.isInheritor(fragmentClass, true))
                || (fragmentV4Class != null && mClass.isInheritor(fragmentV4Class, true))
                || mClass.getName().contains("Fragment");
    }

    /**
     * 创建onCreate方法
     *
     * @param mSelectedText
     * @return
     */
    public static String createOnCreateMethod(String mSelectedText) {
        StringBuilder method = new StringBuilder();
        method.append("@Override protected void onCreate(android.os.Bundle savedInstanceState) {\n");
        method.append("super.onCreate(savedInstanceState);\n");
        method.append("\t// TODO:OnCreate Method has been created, run FindViewById again to generate code\n");
        method.append("\tsetContentView(R.layout.");
        method.append(mSelectedText);
        method.append(");\n");
        method.append("}");
        return method.toString();
    }

    /**
     * 创建onCreateView方法
     *
     * @param mSelectedText
     * @return
     */
    public static String createOnCreateViewMethod(String mSelectedText) {
        StringBuilder method = new StringBuilder();
        method.append("@Override public view onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {\n");
        method.append("\t// TODO:OnCreateView Method has been created, run FindViewById again to generate code\n");
        method.append("\tview view = view.inflate(getActivity(), R.layout.");
        method.append(mSelectedText);
        method.append(", null);");
        method.append("return view;");
        method.append("}");
        return method.toString();
    }

    /**
     * 判断是否实现了OnClickListener接口
     *
     * @param referenceElements
     * @return
     */
    public static boolean isImplementsOnClickListener(PsiJavaCodeReferenceElement[] referenceElements) {
        for (PsiJavaCodeReferenceElement referenceElement : referenceElements) {
            if (referenceElement.getText().contains("OnClickListener")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取initView方法里面的每条数据
     *
     * @param mClass
     * @return
     */
    public static PsiStatement[] getInitViewBodyStatements(PsiClass mClass) {
        // 获取initView方法
        PsiMethod[] method = mClass.findMethodsByName("initView", false);
        PsiStatement[] statements = null;
        if (method.length > 0 && method[0].getBody() != null) {
            PsiCodeBlock methodBody = method[0].getBody();
            statements = methodBody.getStatements();
        }
        return statements;
    }


    /**
     * 获取onClick方法里面的每条数据
     *
     * @param mClass
     * @return
     */
    public static PsiElement[] getOnClickStatement(PsiClass mClass) {
        // 获取onClick方法
        PsiMethod[] onClickMethods = mClass.findMethodsByName("onClick", false);
        PsiElement[] psiElements = null;
        if (onClickMethods.length > 0 && onClickMethods[0].getBody() != null) {
            PsiCodeBlock onClickMethodBody = onClickMethods[0].getBody();
            psiElements = onClickMethodBody.getChildren();
        }
        return psiElements;
    }
}
