from django.db import models

# Create your models here.


class Level(models.Model):
    short_name = models.CharField(max_length=10, unique=True)
    name = models.CharField(max_length=30, unique=True)
    describe = models.TextField()
    need_exp = models.IntegerField()

    def __str__(self):
        return self.short_name


class MethodFamily(models.Model):
    name = models.CharField(max_length=50, unique=True)

    def __str__(self):
        return self.name


class Method(models.Model):
    name = models.TextField(unique=True)
    exp = models.IntegerField()
    method_family = models.ForeignKey(MethodFamily, on_delete=models.CASCADE)

    def __str__(self):
        return self.name


class SkillFamily(models.Model):
    name = models.CharField(max_length=50, unique=True)

    def __str__(self):
        return self.name


class User(models.Model):
    name = models.CharField(max_length=50, unique=True)
    skill_family = models.ManyToManyField(SkillFamily)

    def __str__(self):
        return self.name


class Skill(models.Model):
    name = models.CharField(max_length=30, unique=True)
    tier = models.IntegerField()
    skill_family = models.ForeignKey(SkillFamily, on_delete=models.CASCADE)
    method_family = models.ManyToManyField(MethodFamily)

    def __str__(self):
        return self.name


class UserSkill(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    skill = models.ForeignKey(Skill, on_delete=models.CASCADE)
    skill_exp = models.IntegerField(default=0)
    skill_level = models.ForeignKey(Level, on_delete=models.CASCADE)

    def __str__(self):
        return "user:{},skill:{}".format(self.user, self.skill)


class UserMethod(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    method = models.ForeignKey(Method, on_delete=models.CASCADE)
    skill = models.ForeignKey(Skill, on_delete=models.CASCADE)
    method_times = models.IntegerField(default=0)

    def __str__(self):
        return "user:{},skill:{},method:{}".format(self.user, self.skill,
                                                   self.method)
