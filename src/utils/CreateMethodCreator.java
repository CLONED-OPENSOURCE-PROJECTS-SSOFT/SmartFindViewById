package utils;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction.Simple;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

public class CreateMethodCreator extends Simple {

    private Editor mEditor;
    private PsiFile mFile;
    private Project mProject;
    private PsiClass mClass;
    private PsiElementFactory mFactory;
    private String mSelectedText;
    // activity/fragment
    private String mType;

    public CreateMethodCreator(Editor editor, PsiFile psiFile, PsiClass psiClass, String command, String selectedText, String type) {
        super(psiClass.getProject(), command);
        mEditor = editor;
        mFile = psiFile;
        mProject = psiClass.getProject();
        mClass = psiClass;
        // 获取Factory
        mFactory = JavaPsiFacade.getElementFactory(mProject);
        mSelectedText = selectedText;
        mType = type;
    }

    @Override
    protected void run() throws Throwable {
        try {
            createMethod(mType);
        } catch (Exception e) {
            UIUtils.showPopupBalloon(mEditor, e.getMessage(), 10);
            return;
        }
        // 重写class
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
        styleManager.optimizeImports(mFile);
        styleManager.shortenClassReferences(mClass);
        new ReformatCodeProcessor(mProject, mClass.getContainingFile(), null, false).runWithoutProgress();
        if (mType.equals("activity")) {
            UIUtils.showPopupBalloon(mEditor, "没有OnCreate方法，已创建OnCreate方法，请重新使用FindViewById", 10);
        } else if (mType.equals("fragment")) {
            UIUtils.showPopupBalloon(mEditor, "没有OnCreateView方法，已创建OnCreate方法，请重新使用FindViewById", 10);
        }
    }

    /**
     * 设置Activity的onCreate方法和Fragment的onCreateView方法
     * @param mType activity/fragment
     */
    private void createMethod(String mType) {
        if (AndroidUtils.isAnActivityClass(mClass)) {
            // 判断是否有onCreate方法
            if (mClass.findMethodsByName("onCreate", false).length == 0) {
                // 添加
                mClass.add(mFactory.createMethodFromText(Util.createOnCreateMethod(mSelectedText), mClass));
            }

        } else if (Util.isExtendsFragmentOrFragmentV4(mProject, mClass)) {
            // 判断是否有onCreateView方法
            if (mClass.findMethodsByName("onCreateView", false).length == 0) {
                // 添加
                mClass.add(mFactory.createMethodFromText(Util.createOnCreateViewMethod(mSelectedText), mClass));

            }
        }
    }
}
