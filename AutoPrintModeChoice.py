from PIL import Image


def is_color_image(url):
    img = Image.open(url)
    im = img.resize((200, 200))
    im.save(url + ".png", format='png')
    pix = im.convert('RGB')
    width = im.size[0]
    height = im.size[1]
    oimage_color_type = "Grey Image"
    is_color = []
    total = 0
    colorCnt = 0
    for x in range(width):
        for y in range(height):
            r, g, b = pix.getpixel((x, y))
            r = int(r)
            g = int(g)
            b = int(b)
            if (r == g) and (g == b):
                pass
            elif abs(r - g) < 20 and abs(g - b) < 20 and abs(r - b) < 20:
                pass
            else:
                colorCnt = colorCnt + 1
                # print(r,g,b)
                oimage_color_type = 'Color Image'
            total = total + 1
    print("The rate is", colorCnt, total, colorCnt / total)
    return oimage_color_type


print(is_color_image(".\\1.png"))
