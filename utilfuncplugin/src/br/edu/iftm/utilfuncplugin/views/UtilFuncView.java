package br.edu.iftm.utilfuncplugin.views;


import org.eclipse.swt.widgets.Composite;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.*;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import br.edu.iftm.utilfunc.parser.Parser;
import br.edu.iftm.utilfuncplugin.entity.Function;
import org.eclipse.ui.ide.IDE;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class UtilFuncView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "br.edu.iftm.plugintest.views.UtilFuncView";

	private TableViewer viewer;
	private Action action1;
	private Action doubleClickAction;
	private Composite compoParent;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return new String[] { "One", "Two", "Three" };
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	class NameSorter extends ViewerSorter {
	}
	
	/**
	 * The constructor.
	 */

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());
		// get the content for the viewer, setInput will call getElements in the
		// contentProvider
		// viewer.setInput(ModelProvider.INSTANCE.getPersons());
		// make the selection available to other views
		getSite().setSelectionProvider(viewer);
		// set the sorter for the table

		// define layout for the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				UtilFuncView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());

	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);

	}

	private void makeActions() {
		action1 = new Action() {
			public void run()  {

				try {
					Shell parent = new Shell(SWT.SHELL_TRIM);
					DirectoryDialog dialog = new DirectoryDialog(parent);
					String path = dialog.open();
					if (path != null) {
						parserView(path);
					}
				}catch(Exception e){
						e.printStackTrace();
				}
				
			}
		};
		action1.setText("UtilFunc");
		action1.setToolTipText("Executar UtilFunc");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));


		doubleClickAction = new Action() {
			 

			public void run() {
				
				ISelection selection = viewer.getSelection();
				Function obj = (Function)((IStructuredSelection)selection).getFirstElement();
				
				//showMessage("Caminho: "+obj.getPath());
			    int inicio;
				
				inicio = Integer.parseInt(obj.getLine());	
				
				try {
					IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(obj.getPath()));
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchPage page = window.getActivePage();
					IEditorPart editor = IDE.openEditorOnFileStore(page, fileStore);
					if (editor instanceof ITextEditor) {
		                ITextEditor textEditor = (ITextEditor) editor;

		                if (inicio > 0) {
		                    IDocumentProvider provider = textEditor.getDocumentProvider();
		                    IDocument document = provider.getDocument(textEditor.getEditorInput());

		                    int start = document.getLineOffset(inicio-1); //zero-indexed
		                    textEditor.selectAndReveal(start, 0);
		                }
		            }
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
				viewer.getControl().getShell(),
				"Sample View",
				message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "Path", "Function", "Parameters", "Line" };
		int[] bounds = { 500, 150, 150, 150 };

		// first column is for the path
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Function p = (Function) element;
				return p.getPath();
			}
		});

		// second column is for the function name
		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Function p = (Function) element;
				return p.getFunc();
			}
		});

		//third column is for the parameters
		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Function p = (Function) element;
				return p.getParams();
			}
		});
		
		//Now for the line
		col = createTableViewerColumn(titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Function p = (Function) element;
				return p.getLine();
			}
		});

	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}
	
	public void parserView(String path) {

		String pathFile, func, params, line;
		List<br.edu.iftm.utilfunc.entity.Function> list = new ArrayList();
		Parser p;
		try {
			p = new Parser(path);
			p.parse();
			list = p.getFunctions();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

		Function [] rows = new Function [list.size()];

		for (int i= 0; i < rows.length; i++) {
			func = list.get(i).getName();
			pathFile = list.get(i).getPath();
			line = list.get(i).getLine();
			params = "";
			for(String param : list.get(i).getParams()) {
				params = params + param + ", ";
			}
			if(!params.isEmpty())
		    params = params.substring(0, params.length()-2);
			rows[i] = new Function(pathFile,func,params,line);

		}
		 //Shell shell = new Shell();
		 //Composite unten = new Composite(shell,SWT.NO_BACKGROUND);
		 //createPartControl(unten);
	   	viewer.setInput(rows);
		
	}
}
