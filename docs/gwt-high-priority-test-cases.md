# High Priority GWT Test Cases (60)

Muc tieu: bo test case uu tien cao theo dinh dang Given-When-Then, bao phu translation, date, currency, overflow, RTL, locale switching, encoding, va API consistency.

## A. Translation (10)
1. ID: GWT_TR_01
   Given locale la en
   When mo home page
   Then tat ca key text chinh hien thi dung tieng Anh
2. ID: GWT_TR_02
   Given locale la fr
   When mo home page
   Then khong con cum tu tieng Anh trong header
3. ID: GWT_TR_03
   Given locale la vi
   When mo home page
   Then nhan menu va button duoc dich day du
4. ID: GWT_TR_04
   Given locale la ar
   When mo home page
   Then text UI duoc dich dung tieng Arabic
5. ID: GWT_TR_05
   Given locale la fr
   When mo login form
   Then validation message duoc dich dung
6. ID: GWT_TR_06
   Given locale la vi
   When submit form thieu du lieu
   Then error message la ban dich tieng Viet
7. ID: GWT_TR_07
   Given locale la ar
   When mo cart page
   Then khong co key token chua resolve nhu i18n.xxx
8. ID: GWT_TR_08
   Given locale la fr
   When tim kiem san pham
   Then placeholder va empty-state text duoc dich
9. ID: GWT_TR_09
   Given locale la vi
   When mo product detail
   Then label thong tin san pham duoc dich dung
10. ID: GWT_TR_10
   Given locale la ar
   When doi locale qua lai ar->en->ar
   Then ban dich Arabic van dung va khong bi mat text

## B. Date/Time Format (10)
11. ID: GWT_DT_01
   Given locale la en
   When hien thi ngay tao don
   Then date theo MM/dd/yyyy
12. ID: GWT_DT_02
   Given locale la fr
   When hien thi ngay tao don
   Then date theo dd/MM/yyyy
13. ID: GWT_DT_03
   Given locale la vi
   When hien thi ngay tao don
   Then date theo dd/MM/yyyy
14. ID: GWT_DT_04
   Given locale la ar
   When hien thi ngay tao don
   Then date theo dd/MM/yyyy cho giao dien Arabic
15. ID: GWT_DT_05
   Given locale la fr
   When hien thi thang bang text
   Then ten thang hien thi tieng Phap
16. ID: GWT_DT_06
   Given locale la vi
   When hien thi ngay trong lich su
   Then khong xuat hien ten thang tieng Anh
17. ID: GWT_DT_07
   Given locale la en
   When hien thi gio
   Then format gio dung theo quy uoc locale
18. ID: GWT_DT_08
   Given locale la ar
   When switch qua locale ar
   Then ngay thang duoc cap nhat theo locale moi
19. ID: GWT_DT_09
   Given locale la fr
   When nhap ngay khong hop le
   Then thong bao loi duoc dich va dung ngu canh
20. ID: GWT_DT_10
   Given locale la vi
   When refresh page
   Then dinh dang ngay giu nguyen theo locale da chon

## C. Currency/Number Format (12)
21. ID: GWT_CUR_01
   Given locale la en
   When hien thi gia
   Then symbol tien te hop le va dung vi tri
22. ID: GWT_CUR_02
   Given locale la fr
   When hien thi gia
   Then symbol EUR dung vi tri suffix
23. ID: GWT_CUR_03
   Given locale la vi
   When hien thi gia
   Then symbol VND suffix va grouping dung
24. ID: GWT_CUR_04
   Given locale la ar
   When hien thi gia
   Then symbol locale ar dung vi tri theo rule
25. ID: GWT_CUR_05
   Given locale la fr
   When hien thi so le
   Then decimal separator la comma
26. ID: GWT_CUR_06
   Given locale la en
   When hien thi so le
   Then decimal separator la dot
27. ID: GWT_CUR_07
   Given locale la vi
   When hien thi so lon
   Then grouping separator dung theo locale
28. ID: GWT_CUR_08
   Given locale la ar
   When hien thi tong tien
   Then format tien khong pha tron locale khac
29. ID: GWT_CUR_09
   Given locale la fr
   When hien thi gia am
   Then format so am dung quy tac locale
30. ID: GWT_CUR_10
   Given locale la vi
   When hien thi gia tri 0
   Then format 0 van co dinh dang tien te hop le
31. ID: GWT_CUR_11
   Given locale la en
   When doi locale en->fr
   Then format gia cap nhat ngay lap tuc
32. ID: GWT_CUR_12
   Given locale la ar
   When doi locale ar->vi
   Then khong con symbol/decimal cua Arabic

## D. Long Text and Overflow (10)
33. ID: GWT_OVF_01
   Given locale co chuoi dai
   When hien thi product card
   Then text khong tran khoi card
34. ID: GWT_OVF_02
   Given locale la de/fr voi tu dai
   When hien thi menu
   Then menu khong vo dong bat thuong
35. ID: GWT_OVF_03
   Given locale la vi
   When hien thi thong bao loi dai
   Then text xuong dong dung, khong de len nut
36. ID: GWT_OVF_04
   Given locale la ar
   When hien thi title dai
   Then layout van can doi va khong overlap
37. ID: GWT_OVF_05
   Given mobile viewport
   When mo trang home locale fr
   Then heading dai khong bi cat sai
38. ID: GWT_OVF_06
   Given mobile viewport
   When mo trang home locale ar
   Then search/input khong bi vo bo cuc
39. ID: GWT_OVF_07
   Given desktop viewport
   When hien thi bang thong tin don hang
   Then cot text dai wrap dung
40. ID: GWT_OVF_08
   Given locale la vi
   When hien thi button CTA dai
   Then button khong tran border
41. ID: GWT_OVF_09
   Given locale la ar
   When doi locale nhieu lan
   Then khong xuat hien artifact spacing sai
42. ID: GWT_OVF_10
   Given locale la fr
   When load danh sach nhieu item
   Then khong xuat hien thanh cuon ngang khong mong muon

## E. RTL/LTR and Switching (10)
43. ID: GWT_RTL_01
   Given locale la ar
   When mo page
   Then html dir = rtl
44. ID: GWT_RTL_02
   Given locale la en
   When mo page
   Then html dir khong phai rtl
45. ID: GWT_RTL_03
   Given locale la ar
   When quan sat icon dieu huong
   Then icon huong duoc mirror dung
46. ID: GWT_RTL_04
   Given locale la ar
   When nhap text vao input
   Then text align right dung mong doi
47. ID: GWT_RTL_05
   Given locale la ar
   When hien thi so va chu latin trong cung dong
   Then rendering on dinh, khong vo thu tu
48. ID: GWT_RTL_06
   Given locale la fr
   When switch fr->ar
   Then direction doi sang rtl trong 1 chu ky load
49. ID: GWT_RTL_07
   Given locale la ar
   When switch ar->en
   Then direction tra ve ltr va reset style
50. ID: GWT_RTL_08
   Given locale la ar
   When refresh page
   Then dir=rtl duoc giu nguyen
51. ID: GWT_RTL_09
   Given locale la en
   When switch en->ar->en lien tuc
   Then khong con sot class/style rtl o en
52. ID: GWT_RTL_10
   Given locale la ar
   When mo modal hoac menu dropdown
   Then canh le va huong menu dung rtl

## F. Encoding/API Consistency (8)
53. ID: GWT_ENC_01
   Given locale la vi
   When render text co dau
   Then khong co ky tu vo dang mojibake
54. ID: GWT_ENC_02
   Given locale la fr
   When render text co accent
   Then accent hien thi chinh xac
55. ID: GWT_ENC_03
   Given locale la ar
   When render glyph Arabic
   Then tat ca ky tu hien thi dung font va thu tu
56. ID: GWT_ENC_04
   Given locale la vi
   When copy/paste text localization
   Then gia tri giu nguyen khong loi encoding
57. ID: GWT_API_01
   Given request co locale=fr
   When goi API data
   Then truong localized tra ve theo fr
58. ID: GWT_API_02
   Given request co locale=ar
   When goi API data
   Then response khong chua fallback text tieng Anh
59. ID: GWT_API_03
   Given UI locale la vi
   When hien thi gia va ngay tu API
   Then format UI khop profile locale vi
60. ID: GWT_API_04
   Given locale khong ho tro
   When goi API
   Then he thong fallback locale mac dinh va log canh bao

## Huong dan uu tien triage
- Priority P1: case 11-14, 21-28, 33-38, 43-50, 53-56.
- Priority P2: cac case con lai.
- P1 fail: chan release.
