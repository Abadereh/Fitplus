from pathlib import Path
import sys
p=Path(sys.argv[1])
s=p.read_text(encoding='utf-8')
start=s.index('.method private showProfileDetail(J)V')
end=s.index('.end method', start)+len('.end method')
method=s[start:end]
anchor='    invoke-interface {v14}, Ljava/util/List;->iterator()Ljava/util/Iterator;'
if anchor not in method:
    raise SystemExit('showProfileDetail checklist iterator anchor not found')
insert='''    # v1.0.15 quick checklist router
    invoke-static {v0, v6, v1, v2, v14}, Lcom/aoto/mozarebe/QuickChecklistHelper;->render(Landroid/app/Activity;Landroid/widget/LinearLayout;JLjava/util/List;)Z

    move-result v5

    if-nez v5, :cond_6

'''
method2=method.replace(anchor, insert+anchor, 1)
s=s[:start]+method2+s[end:]
p.write_text(s, encoding='utf-8')
print('patched', p)
