" Vim syntax file
" Language: Lox
" Maintainer: Nicolay Caspersen Roness
"
" Usage:
"   Put in $VIMRUNTIME/syntax/lox.vim
"   Add command:
"     autocmd BufRead,BufNewFile *.lox set filetype=lox

if exists("b:current_syntax")
    finish
endif

" Keywords
syntax keyword loxKeywords var print if else or and for while fun return class this init

" String literals
syntax region loxString start=/\v"/ skip=/\v\\./ end=/\v"/

" Comments
syntax region loxComment start="//" end="$"
syntax region loxCommentBlock start="/\*" end="\*/"

highlight default link loxKeywords Keyword
highlight default link loxString String
highlight default link loxComment Comment
highlight default link loxCommentBlock  Comment

let b:current_syntax = "lox"
